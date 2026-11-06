import { api, BASE_API_URL, getAuthToken } from './client';
import { Stage } from '../types/stage';

export interface ChatHistoryItem {
  sender: 'User' | 'Model';
  content: string;
}

export interface StageChatTurnRequest {
  message?: string;
  history?: ChatHistoryItem[];
  userLevel?: string;
}

export interface StageChatTurnResult {
  message: string;
  translatedMessage?: string | null;
  audioUrl?: string | null;
  hasGrammarError?: boolean;
  correctedSentence?: string | null;
  grammarFeedbackFa?: string | null;
  objectiveCompleted?: boolean;
}

export interface HintRequest {
  messages?: { role: string; content: string }[];
  userLevel?: string;
}

export interface HintResponse {
  suggestion: string;
  explanationFa: string;
}

export interface GrammarErrorItem {
  original: string;
  correction: string;
  explanationFa: string;
}

export interface EvaluationRequest {
  objectiveCompleted?: boolean;
  hintsUsedCount?: number;
  turnsCount?: number;
  grammarErrorsCount?: number;
  grammarErrors?: GrammarErrorItem[];
  transcript?: { role: string; content: string }[];
}

export interface EvaluationResponse {
  stageId: string;
  objectiveCompleted: boolean;
  grammarErrorsCount: number;
  hintsUsedCount: number;
  totalPenalties: number;
  starsEarned: number;
  score: number;
  isHighScore: boolean;
  unlockedNextStage: boolean;
  grammarErrors: GrammarErrorItem[];
  feedbackFa: string;
}

export const chatApi = {
  /**
   * Retrieves single stage detail with target objectives, briefings, and character info.
   */
  async getStageDetail(stageId: string): Promise<Stage> {
    return api.get<Stage>(`/v2/stages/${stageId}`);
  },

  /**
   * Requests conversational hint and guidance in Persian & English.
   */
  async requestHint(stageId: string, request: HintRequest): Promise<HintResponse> {
    return api.post<HintResponse>(`/v2/stages/${stageId}/hint`, request);
  },

  /**
   * Standard synchronous chat turn.
   */
  async sendChatMessage(
    stageId: string,
    request: StageChatTurnRequest
  ): Promise<StageChatTurnResult> {
    return api.post<StageChatTurnResult>(`/v2/stages/${stageId}/chat`, request);
  },

  /**
   * Transcribes a recorded WAV audio blob via server-side Sherpa-ONNX STT.
   */
  async transcribeAudio(wavBlob: Blob): Promise<{ text: string; durationMs: number }> {
    const token = getAuthToken();
    const headers: Record<string, string> = {
      'Content-Type': 'audio/wav',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${BASE_API_URL}/v2/stt/transcribe`, {
      method: 'POST',
      headers,
      body: wavBlob,
    });

    if (!response.ok) {
      throw new Error(`STT transcribe failed with status ${response.status}`);
    }

    const json = await response.json();
    return json.data || { text: '', durationMs: 0 };
  },

  /**
   * Evaluates final conversation, awards stars, and saves progress.
   */
  async submitEvaluation(
    stageId: string,
    request: EvaluationRequest
  ): Promise<EvaluationResponse> {
    return api.post<EvaluationResponse>(`/v2/stages/${stageId}/evaluate`, request);
  },

  /**
   * Streams chat response using SSE (Server-Sent Events) via fetch stream reader.
   * Gracefully falls back to synchronous chat if fetch streaming fails.
   */
  async streamChatMessage(
    stageId: string,
    request: StageChatTurnRequest,
    callbacks: {
      onChunk: (delta: string) => void;
      onDone: (result: StageChatTurnResult) => void;
      onError: (error: Error) => void;
    }
  ): Promise<() => void> {
    const controller = new AbortController();
    const token = getAuthToken();

    (async () => {
      try {
        const url = `${BASE_API_URL}/v2/stages/${stageId}/chat/stream`;
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
        };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, {
          method: 'POST',
          headers,
          body: JSON.stringify(request),
          signal: controller.signal,
        });

        if (!response.ok || !response.body) {
          throw new Error(`Streaming failed with status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            const trimmed = line.trim();
            if (trimmed.startsWith('data:')) {
              const dataContent = trimmed.substring(5).trim();
              if (!dataContent) continue;

              try {
                const parsed = JSON.parse(dataContent);
                if (parsed.type === 'chunk' && typeof parsed.delta === 'string') {
                  callbacks.onChunk(parsed.delta);
                } else if (parsed.type === 'done' && parsed.payload) {
                  callbacks.onDone(parsed.payload as StageChatTurnResult);
                  return;
                } else if (parsed.type === 'error') {
                  throw new Error(parsed.message || 'Server streaming error');
                }
              } catch (parseErr) {
                if (parseErr instanceof SyntaxError) {
                  // Ignore keepalive ping or partial data
                  continue;
                }
                throw parseErr;
              }
            }
          }
        }
      } catch (err) {
        if ((err as Error)?.name === 'AbortError') {
          return;
        }

        // Fallback to synchronous turn if streaming fails
        console.warn('Chat SSE streaming failed, falling back to sync chat', err);
        try {
          const fallbackResult = await chatApi.sendChatMessage(stageId, request);
          callbacks.onDone(fallbackResult);
        } catch (syncErr) {
          callbacks.onError(syncErr as Error);
        }
      }
    })();

    return () => {
      controller.abort();
    };
  },
};
