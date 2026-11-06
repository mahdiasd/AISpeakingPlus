import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Target, Flag, Sparkles, RefreshCw } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { CupertinoNavBar, CupertinoSkeleton } from '../components/cupertino';
import { AiMessageItem } from '../components/chat/AiMessageItem';
import { UserMessageItem } from '../components/chat/UserMessageItem';
import { ChatBottomBar } from '../components/chat/ChatBottomBar';
import { ObjectiveModal } from '../components/chat/ObjectiveModal';
import { HintModal } from '../components/chat/HintModal';
import { EvaluationModal } from '../components/chat/EvaluationModal';
import { ExitConfirmModal } from '../components/chat/ExitConfirmModal';
import {
  chatApi,
  StageChatTurnResult,
  HintResponse,
  EvaluationResponse,
  GrammarErrorItem,
} from '../api/chatApi';
import { stageApi, saveLocalGuestProgress } from '../api/stageApi';
import { Stage } from '../types/stage';
import { useSpeechRecognition } from '../hooks/useSpeechRecognition';
import { useAudioPlayer } from '../hooks/useAudioPlayer';

interface ChatBubble {
  id: string;
  sender: 'Model' | 'User';
  text: string;
  translatedMessage?: string | null;
  audioUrl?: string | null;
  hasGrammarError?: boolean;
  correctedSentence?: string | null;
  grammarFeedbackFa?: string | null;
  isStreaming?: boolean;
}

export const ChatPage: React.FC = () => {
  const { stageId } = useParams<{ stageId: string }>();
  const navigate = useNavigate();

  const [stage, setStage] = useState<Stage | null>(null);
  const [isLoadingStage, setIsLoadingStage] = useState(true);
  const [stageLoadError, setStageLoadError] = useState<string | null>(null);

  const [chats, setChats] = useState<ChatBubble[]>([]);
  const [turnsCount, setTurnsCount] = useState(0);
  const [hintsUsedCount, setHintsUsedCount] = useState(0);
  const [grammarErrorsCollected, setGrammarErrorsCollected] = useState<GrammarErrorItem[]>([]);
  const [isObjectiveCompleted, setIsObjectiveCompleted] = useState(false);

  const [isModelSpeaking, setIsModelSpeaking] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [isLoadingHint, setIsLoadingHint] = useState(false);

  // Modals state
  const [isObjectiveOpen, setIsObjectiveOpen] = useState(false);
  const [isHintOpen, setIsHintOpen] = useState(false);
  const [hintData, setHintData] = useState<HintResponse | null>(null);
  const [isExitConfirmOpen, setIsExitConfirmOpen] = useState(false);
  const [isEvaluationOpen, setIsEvaluationOpen] = useState(false);
  const [evaluationData, setEvaluationData] = useState<EvaluationResponse | null>(null);
  const [isSubmittingEvaluation, setIsSubmittingEvaluation] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const audioPlayer = useAudioPlayer();

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((prev) => (prev === msg ? null : prev));
    }, 3500);
  };

  // 1. Initialize Stage Data
  useEffect(() => {
    if (!stageId) return;

    let isMounted = true;
    setIsLoadingStage(true);
    setStageLoadError(null);

    chatApi
      .getStageDetail(stageId)
      .then((data) => {
        if (!isMounted) return;
        setStage(data);
        setIsLoadingStage(false);

        // If stage starts with AI greeting, request initial message
        if (data.initialSpeaker === 'Model') {
          fetchInitialAiGreeting(stageId, data);
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        console.error('Failed to load stage detail', err);
        setStageLoadError('خطا در دریافت اطلاعات مرحله. لطفاً اتصال اینترنت خود را بررسی کنید.');
        setIsLoadingStage(false);
      });

    return () => {
      isMounted = false;
      audioPlayer.stop();
    };
  }, [stageId]);

  // Auto-scroll to bottom of conversation
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chats, isModelSpeaking]);

  // 2. Fetch Initial AI Greeting
  const fetchInitialAiGreeting = async (targetStageId: string, currentStage: Stage) => {
    setIsModelSpeaking(true);
    const aiMessageId = `ai_init_${Date.now()}`;

    // Add empty placeholder bubble
    setChats([
      {
        id: aiMessageId,
        sender: 'Model',
        text: '',
        isStreaming: true,
      },
    ]);

    try {
      const response = await chatApi.sendChatMessage(targetStageId, {
        history: [],
      });

      setChats([
        {
          id: aiMessageId,
          sender: 'Model',
          text: response.message,
          translatedMessage: response.translatedMessage,
          audioUrl: response.audioUrl,
          isStreaming: false,
        },
      ]);

      // Play greeting audio
      if (response.message) {
        audioPlayer.play(aiMessageId, response.message, response.audioUrl);
      }
    } catch (err) {
      console.warn('Initial AI greeting failed, using stage fallback briefing', err);
      const fallbackGreeting =
        currentStage.briefing || `Hello! Welcome to ${currentStage.title}. How can I help you today?`;

      setChats([
        {
          id: aiMessageId,
          sender: 'Model',
          text: fallbackGreeting,
          translatedMessage: currentStage.briefingFa,
          isStreaming: false,
        },
      ]);
    } finally {
      setIsModelSpeaking(false);
    }
  };

  // 3. Send User Message & Handle AI Response
  const handleSendMessage = async (userText: string) => {
    if (!stageId || !stage || isSending || isModelSpeaking) return;

    const userMessageId = `user_${Date.now()}`;
    const newTurnsCount = turnsCount + 1;
    setTurnsCount(newTurnsCount);

    // Append user message immediately
    const updatedChats: ChatBubble[] = [
      ...chats,
      {
        id: userMessageId,
        sender: 'User',
        text: userText,
      },
    ];

    setChats(updatedChats);
    setIsSending(true);
    setIsModelSpeaking(true);

    const aiMessageId = `ai_${Date.now()}`;
    // Add AI placeholder bubble
    setChats((prev) => [
      ...prev,
      {
        id: aiMessageId,
        sender: 'Model',
        text: '',
        isStreaming: true,
      },
    ]);

    // Build dialogue history for server
    const historyPayload = updatedChats.map((c) => ({
      sender: (c.sender === 'User' ? 'User' : 'Model') as 'User' | 'Model',
      content: c.text,
    }));

    try {
      // Stream AI response via chatApi
      let accumulatedDelta = '';
      await chatApi.streamChatMessage(
        stageId,
        {
          message: userText,
          history: historyPayload,
        },
        {
          onChunk: (delta) => {
            accumulatedDelta += delta;
            setChats((prev) =>
              prev.map((c) => (c.id === aiMessageId ? { ...c, text: accumulatedDelta } : c))
            );
          },
          onDone: (turnResult: StageChatTurnResult) => {
            // Update user message with grammar feedback if error detected
            if (turnResult.hasGrammarError) {
              setChats((prev) =>
                prev.map((c) =>
                  c.id === userMessageId
                    ? {
                        ...c,
                        hasGrammarError: true,
                        correctedSentence: turnResult.correctedSentence,
                        grammarFeedbackFa: turnResult.grammarFeedbackFa,
                      }
                    : c
                )
              );

              setGrammarErrorsCollected((prev) => [
                ...prev,
                {
                  original: userText,
                  correction: turnResult.correctedSentence || userText,
                  explanationFa: turnResult.grammarFeedbackFa || 'نکته گرامری در ساختار جمله',
                },
              ]);
            }

            // Update AI message with final payload
            setChats((prev) =>
              prev.map((c) =>
                c.id === aiMessageId
                  ? {
                      ...c,
                      text: turnResult.message,
                      translatedMessage: turnResult.translatedMessage,
                      audioUrl: turnResult.audioUrl,
                      isStreaming: false,
                    }
                  : c
              )
            );

            if (turnResult.objectiveCompleted) {
              setIsObjectiveCompleted(true);
              showToast('هدف ماموریت مرحله با موفقیت تکمیل شد! آفرین!');
            }

            // Auto-play audio voice
            if (turnResult.message) {
              audioPlayer.play(aiMessageId, turnResult.message, turnResult.audioUrl);
            }

            setIsSending(false);
            setIsModelSpeaking(false);
          },
          onError: (err) => {
            console.error('Error during chat turn', err);
            setChats((prev) =>
              prev.map((c) =>
                c.id === aiMessageId
                  ? {
                      ...c,
                      text: 'Sorry, I had trouble connecting. Could you please say that again?',
                      isStreaming: false,
                    }
                  : c
              )
            );
            setIsSending(false);
            setIsModelSpeaking(false);
          },
        }
      );
    } catch (err) {
      console.error('Failed to send message', err);
      setIsSending(false);
      setIsModelSpeaking(false);
    }
  };

  // 4. Speech Recognition Integration
  const speechRecognition = useSpeechRecognition((finalTranscript) => {
    if (finalTranscript.trim()) {
      handleSendMessage(finalTranscript.trim());
    }
  });

  // 5. Request Stage Hint
  const handleRequestHint = async () => {
    if (!stageId || isLoadingHint) return;

    setIsLoadingHint(true);
    setHintsUsedCount((prev) => prev + 1);

    const historyPayload = chats.map((c) => ({
      role: c.sender === 'User' ? 'user' : 'assistant',
      content: c.text,
    }));

    try {
      const hint = await chatApi.requestHint(stageId, {
        messages: historyPayload,
      });

      setHintData(hint);
      setIsHintOpen(true);
    } catch (err) {
      console.error('Failed to get hint', err);
      showToast('خطا در دریافت راهنما. لطفاً دوباره تلاش کنید.');
    } finally {
      setIsLoadingHint(false);
    }
  };

  // 6. Finish Conversation & Submit Evaluation
  const handleFinishConversation = async () => {
    if (!stageId || !stage || isSubmittingEvaluation) return;

    setIsSubmittingEvaluation(true);
    audioPlayer.stop();

    const transcriptPayload = chats.map((c) => ({
      role: c.sender === 'User' ? 'user' : 'assistant',
      content: c.text,
    }));

    try {
      const result = await chatApi.submitEvaluation(stageId, {
        objectiveCompleted: isObjectiveCompleted || turnsCount >= 2,
        hintsUsedCount,
        turnsCount,
        grammarErrorsCount: grammarErrorsCollected.length,
        grammarErrors: grammarErrorsCollected,
        transcript: transcriptPayload,
      });

      setEvaluationData(result);
      setIsEvaluationOpen(true);

      // Persist progress locally for guest sessions
      saveLocalGuestProgress(stageId, {
        stars: result.starsEarned,
        bestScore: result.score,
        completedAt: new Date().toISOString(),
      });
    } catch (err) {
      console.error('Failed to submit evaluation', err);
      showToast('خطا در ارسال نتیجه ارزیابی. لطفاً دوباره تلاش کنید.');
    } finally {
      setIsSubmittingEvaluation(false);
    }
  };

  // 7. Navigation Actions
  const handleBackClick = () => {
    if (chats.length > 1) {
      setIsExitConfirmOpen(true);
    } else {
      navigate('/');
    }
  };

  const handleNextStage = async () => {
    if (!stage) return;
    try {
      const catalog = await stageApi.getStages();
      const currentIdx = catalog.stages.findIndex((s) => s.id === stage.id);
      const nextStage = catalog.stages[currentIdx + 1];

      if (nextStage) {
        setIsEvaluationOpen(false);
        navigate(`/chat/${nextStage.id}`);
      } else {
        navigate('/');
      }
    } catch {
      navigate('/');
    }
  };

  const handleRetryStage = () => {
    setIsEvaluationOpen(false);
    setChats([]);
    setTurnsCount(0);
    setHintsUsedCount(0);
    setGrammarErrorsCollected([]);
    setIsObjectiveCompleted(false);
    if (stage && stage.initialSpeaker === 'Model' && stageId) {
      fetchInitialAiGreeting(stageId, stage);
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#0c0c0e] text-white selection:bg-apple-blue selection:text-white pb-32">
      {/* 1. Cupertino Top App Bar */}
      <CupertinoNavBar
        title={stage ? stage.titleFa : 'اتاق مکالمه هوشمند'}
        subtitle={
          stage
            ? `نوبت ${turnsCount} از ${stage.maxTurns || 10} • ${stage.characterName}`
            : 'در حال بارگذاری...'
        }
        onBack={handleBackClick}
        endSlot={
          <div className="flex items-center gap-1.5">
            {/* Objective Modal Button */}
            <button
              type="button"
              onClick={() => setIsObjectiveOpen(true)}
              className="p-2 rounded-2xl bg-white/10 hover:bg-white/15 text-apple-blue active:scale-95 transition-all"
              title="مشاهده اهداف مرحله"
            >
              <Target className="w-5 h-5" />
            </button>

            {/* Finish Conversation Button */}
            <button
              type="button"
              onClick={handleFinishConversation}
              disabled={chats.length === 0 || isSubmittingEvaluation}
              className="flex items-center gap-1 px-3 py-1.5 rounded-2xl bg-apple-green/20 hover:bg-apple-green/30 text-apple-green text-xs font-bold active:scale-95 disabled:opacity-40 transition-all border border-apple-green/30"
              title="پایان مکالمه و دریافت کارنامه"
            >
              {isSubmittingEvaluation ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <Flag className="w-3.5 h-3.5" />
              )}
              <span className="hidden sm:inline">پایان مرحله</span>
            </button>
          </div>
        }
      />

      {/* 2. Main Chat Conversation Feed */}
      <main className="flex-1 max-w-lg w-full mx-auto px-3.5 pt-4">
        {/* Loading State */}
        {isLoadingStage && (
          <div className="space-y-4 pt-6">
            <CupertinoSkeleton className="h-24 rounded-3xl" />
            <CupertinoSkeleton className="h-20 rounded-3xl w-4/5" />
            <CupertinoSkeleton className="h-20 rounded-3xl w-3/4 mr-auto" />
          </div>
        )}

        {/* Error State */}
        {stageLoadError && (
          <div className="pt-12 text-center space-y-4">
            <div className="p-4 rounded-3xl bg-apple-red/10 border border-apple-red/20 text-slate-200 text-sm">
              {stageLoadError}
            </div>
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="px-5 py-2.5 rounded-2xl bg-white/10 hover:bg-white/15 text-sm font-bold text-white active:scale-95 transition-all"
            >
              تلاش مجدد
            </button>
          </div>
        )}

        {/* Conversation Bubbles */}
        {!isLoadingStage && stage && (
          <div className="space-y-2">
            {/* Stage Ambient Header Card */}
            <div className="relative rounded-3xl overflow-hidden p-4 mb-4 border border-white/10 apple-glass">
              {stage.backgroundUrl && (
                <img
                  src={stage.backgroundUrl}
                  alt={stage.title}
                  className="absolute inset-0 w-full h-full object-cover opacity-20 filter blur-sm"
                />
              )}
              <div className="relative z-10 flex items-center justify-between">
                <div>
                  <span className="text-[11px] text-apple-teal font-semibold block">
                    {stage.seasonTitleFa} • {stage.phaseTitleFa}
                  </span>
                  <h2 className="text-sm font-bold text-white">{stage.titleFa}</h2>
                </div>

                <button
                  type="button"
                  onClick={() => setIsObjectiveOpen(true)}
                  className="text-xs px-2.5 py-1 rounded-full bg-apple-blue/20 text-apple-blue border border-apple-blue/30 font-bold hover:bg-apple-blue/30 transition-all"
                >
                  راهنمای سناریو
                </button>
              </div>
            </div>

            {/* Messages List */}
            {chats.map((chat) =>
              chat.sender === 'Model' ? (
                <AiMessageItem
                  key={chat.id}
                  id={chat.id}
                  message={chat.text}
                  translatedMessage={chat.translatedMessage}
                  audioUrl={chat.audioUrl}
                  characterName={stage.characterName}
                  characterAvatarUrl={stage.characterAvatarUrl}
                  isStreaming={chat.isStreaming}
                  isPlaying={audioPlayer.isPlaying(chat.id)}
                  onPlayAudio={() => audioPlayer.play(chat.id, chat.text, chat.audioUrl)}
                />
              ) : (
                <UserMessageItem
                  key={chat.id}
                  id={chat.id}
                  message={chat.text}
                  hasGrammarError={chat.hasGrammarError}
                  correctedSentence={chat.correctedSentence}
                  grammarFeedbackFa={chat.grammarFeedbackFa}
                />
              )
            )}

            <div ref={messagesEndRef} />
          </div>
        )}
      </main>

      {/* 3. Floating Bottom Controls & Speech Dock */}
      {!isLoadingStage && stage && (
        <ChatBottomBar
          onSendMessage={handleSendMessage}
          onRequestHint={handleRequestHint}
          isModelSpeaking={isModelSpeaking}
          isSending={isSending}
          isLoadingHint={isLoadingHint}
          isRecording={speechRecognition.isListening}
          recordingTranscript={speechRecognition.transcript}
          onStartRecording={speechRecognition.startListening}
          onStopRecording={speechRecognition.stopListening}
          onCancelRecording={speechRecognition.resetTranscript}
          recordingError={speechRecognition.error}
        />
      )}

      {/* 4. Objective Modal */}
      <ObjectiveModal
        isOpen={isObjectiveOpen}
        onClose={() => setIsObjectiveOpen(false)}
        stage={stage}
      />

      {/* 5. Hint Suggestion Modal */}
      {hintData && (
        <HintModal
          isOpen={isHintOpen}
          onClose={() => setIsHintOpen(false)}
          suggestion={hintData.suggestion}
          explanationFa={hintData.explanationFa}
          onUseSuggestion={(text) => handleSendMessage(text)}
          onPlayAudio={(text) => audioPlayer.play('hint_audio', text)}
        />
      )}

      {/* 6. Evaluation Result Modal */}
      <EvaluationModal
        isOpen={isEvaluationOpen}
        onClose={() => setIsEvaluationOpen(false)}
        evaluation={evaluationData}
        stageTitle={stage?.titleFa}
        onNextStage={handleNextStage}
        onRetryStage={handleRetryStage}
        onBackToMap={() => navigate('/')}
      />

      {/* 7. Exit Confirmation Modal */}
      <ExitConfirmModal
        isOpen={isExitConfirmOpen}
        onClose={() => setIsExitConfirmOpen(false)}
        onConfirmExit={() => {
          setIsExitConfirmOpen(false);
          navigate('/');
        }}
      />

      {/* 8. Toast Notification */}
      <AnimatePresence>
        {toastMessage && (
          <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 16, scale: 0.95 }}
            className="fixed top-20 inset-x-4 max-w-sm mx-auto z-50 pointer-events-none"
          >
            <div className="apple-glass bg-[#1b1e2c]/95 border border-white/20 text-white text-xs px-4 py-3 rounded-2xl shadow-apple-lg flex items-center gap-2 pointer-events-auto">
              <Sparkles className="w-4 h-4 text-apple-yellow shrink-0" />
              <span>{toastMessage}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default ChatPage;
