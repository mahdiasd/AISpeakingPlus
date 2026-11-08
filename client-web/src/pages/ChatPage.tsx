import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Sparkles } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { CupertinoSkeleton } from '../components/cupertino';
import { ChatToolbar } from '../components/chat/ChatToolbar';
import { AiMessageItem } from '../components/chat/AiMessageItem';
import { UserMessageItem } from '../components/chat/UserMessageItem';
import { ChatBottomBar } from '../components/chat/ChatBottomBar';
import { StageInfoCompanion } from '../components/chat/StageInfoCompanion';
import { LiveTelemetryCompanion } from '../components/chat/LiveTelemetryCompanion';
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
import { useAuth } from '../hooks/useAuth';

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
  isEvaluatingGrammar?: boolean;
}

export const ChatPage: React.FC = () => {
  const { stageId } = useParams<{ stageId: string }>();
  const navigate = useNavigate();
  const { profile } = useAuth();

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

      if (response.message) {
        audioPlayer.play(aiMessageId, response.message, response.audioUrl);
      }
    } catch (err) {
      console.warn('Initial AI greeting failed, using fallback briefing', err);
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

    const updatedChats: ChatBubble[] = [
      ...chats,
      {
        id: userMessageId,
        sender: 'User',
        text: userText,
        isEvaluatingGrammar: true,
      },
    ];

    setChats(updatedChats);
    setIsSending(true);
    setIsModelSpeaking(true);

    const aiMessageId = `ai_${Date.now()}`;
    setChats((prev) => [
      ...prev,
      {
        id: aiMessageId,
        sender: 'Model',
        text: '',
        isStreaming: true,
      },
    ]);

    const historyPayload = updatedChats.map((c) => ({
      sender: (c.sender === 'User' ? 'User' : 'Model') as 'User' | 'Model',
      content: c.text,
    }));

    try {
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
            setChats((prev) =>
              prev.map((c) =>
                c.id === userMessageId
                  ? {
                      ...c,
                      isEvaluatingGrammar: false,
                      hasGrammarError: Boolean(turnResult.hasGrammarError),
                      correctedSentence: turnResult.correctedSentence,
                      grammarFeedbackFa: turnResult.grammarFeedbackFa,
                    }
                  : c
              )
            );

            if (turnResult.hasGrammarError) {
              setGrammarErrorsCollected((prev) => [
                ...prev,
                {
                  original: userText,
                  correction: turnResult.correctedSentence || userText,
                  explanationFa: turnResult.grammarFeedbackFa || 'نکته گرامری در ساختار جمله',
                },
              ]);
            }

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
              showToast('هدف ماموریت مرحله با موفقیت تکمیل شد!');
            }

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

  // 4. Robust Speech Recognition & Voice Recording Integration
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
    <div className="relative h-[100dvh] max-h-[100dvh] w-full bg-[#07080f] text-white selection:bg-apple-blue selection:text-white overflow-hidden flex flex-col justify-between">
      {/* 1. Fullscreen Ambient Stage Wallpaper with Liquid Glass Refraction & Light Orbs */}
      {stage && stage.backgroundUrl ? (
        <div className="fixed inset-0 z-0 select-none pointer-events-none overflow-hidden">
          <img
            src={stage.backgroundUrl}
            alt={stage.title}
            className="w-full h-full object-cover object-center filter blur-3xl brightness-[0.55] contrast-[1.1] scale-110 transform-gpu"
          />
          {/* Dynamic Optical Light Orbs for Liquid Glass */}
          <div className="absolute -top-16 -right-16 w-80 h-80 rounded-full bg-apple-blue/25 blur-[100px] pointer-events-none" />
          <div className="absolute top-1/2 -left-20 w-80 h-80 rounded-full bg-apple-purple/20 blur-[110px] pointer-events-none" />
          <div className="absolute inset-0 bg-gradient-to-b from-[#07080f]/85 via-[#0b0e1b]/70 to-[#06070e]/88" />
        </div>
      ) : (
        <div className="fixed inset-0 z-0 bg-gradient-to-b from-[#07080f] via-[#0b0e1b] to-[#06070e]">
          <div className="absolute -top-16 -right-16 w-80 h-80 rounded-full bg-apple-blue/25 blur-[100px]" />
          <div className="absolute bottom-10 -left-10 w-80 h-80 rounded-full bg-apple-purple/25 blur-[110px]" />
        </div>
      )}

      {/* 2. Responsive Multi-Column Stage Layout */}
      <div className="relative z-10 w-full flex-1 min-h-0 flex items-center justify-center lg:py-6 lg:px-6">
        <div className="w-full max-w-7xl flex items-center justify-center lg:gap-8 h-full">
          {/* A. Right Companion (in RTL: Stage Briefing & Character Info) */}
          <aside className="hidden lg:block shrink-0">
            <StageInfoCompanion
              stage={stage}
              userLevel={profile?.languageLevel || 'A1'}
              isObjectiveCompleted={isObjectiveCompleted}
              onOpenObjectiveModal={() => setIsObjectiveOpen(true)}
              onPlayBriefingAudio={() => {
                if (stage?.briefing) {
                  audioPlayer.play('stage_briefing', stage.briefing);
                }
              }}
            />
          </aside>

          {/* B. Center Mobile Canvas (Authentic Apple Device Mockup on Desktop, Full Bleed on Mobile) */}
          <section className="relative w-full lg:max-w-[430px] h-[100dvh] lg:h-[88vh] lg:max-h-[900px] flex flex-col bg-[#0b0d17]/55 lg:rounded-[44px] overflow-hidden lg:border-[2px] lg:border-white/20 lg:shadow-[0_25px_70px_rgba(0,0,0,0.8)] backdrop-blur-2xl">
            {/* Specular Rim Light on Desktop Canvas */}
            <div
              aria-hidden="true"
              className="hidden lg:block absolute inset-x-0 top-0 h-[2px] apple-specular-edge pointer-events-none z-40 rounded-t-[44px]"
            />

            {/* Desktop Dynamic Island Notch */}
            <div className="hidden lg:flex absolute top-2 inset-x-0 justify-center z-40 pointer-events-none">
              <div className="w-28 h-5 rounded-full bg-black border border-white/10 shadow-inner flex items-center justify-end px-3">
                <span className="w-2.5 h-2.5 rounded-full bg-[#111] border border-white/15" />
              </div>
            </div>

            {/* Native Mobile Stage Wallpaper inside Canvas */}
            {stage && stage.backgroundUrl && (
              <div className="absolute inset-0 z-0 pointer-events-none overflow-hidden select-none">
                <img
                  src={stage.backgroundUrl}
                  alt={stage.title}
                  className="w-full h-full object-cover object-top filter brightness-[0.45] contrast-[1.08]"
                />
                <div className="absolute inset-0 bg-gradient-to-b from-[#090b16]/80 via-[#0e1122]/70 to-[#080912]/85" />
              </div>
            )}

            {/* Header: Apple Chat Toolbar */}
            <header className="shrink-0 pt-[max(env(safe-area-inset-top),6px)] lg:pt-3 z-30 bg-[#0c0e1a]/40 backdrop-blur-[14px] border-b border-white/10">
              <ChatToolbar
                stage={stage}
                turnsCount={turnsCount}
                maxTurns={stage?.maxTurns || 10}
                userLevel={profile?.languageLevel || 'A1'}
                isFinishing={isSubmittingEvaluation}
                onBackClick={handleBackClick}
                onObjectiveClick={() => setIsObjectiveOpen(true)}
                onFinishClick={handleFinishConversation}
              />
            </header>

            {/* Main Chat Conversation Feed (ONLY this element scrolls on mobile) */}
            <main className="relative z-10 flex-1 min-h-0 overflow-y-auto overscroll-contain px-3.5 pt-3 pb-4 space-y-4">
              {/* Loading Skeleton */}
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

              {/* Conversation Messages */}
              {!isLoadingStage && stage && (
                <div className="space-y-3 pb-4" dir="ltr">
                  {chats.map((chat) =>
                    chat.sender === 'Model' ? (
                      <div key={chat.id} className="w-full flex justify-start">
                        <AiMessageItem
                          id={chat.id}
                          message={chat.text}
                          translatedMessage={chat.translatedMessage}
                          audioUrl={chat.audioUrl}
                          characterName={stage.characterName}
                          characterAvatarUrl={stage.characterAvatarUrl}
                          isStreaming={chat.isStreaming}
                          isPlaying={audioPlayer.isPlaying(chat.id)}
                          onPlayAudio={() => audioPlayer.play(chat.id, chat.text, chat.audioUrl)}
                          onStopAudio={() => audioPlayer.stop()}
                        />
                      </div>
                    ) : (
                      <div key={chat.id} className="w-full flex justify-end">
                        <UserMessageItem
                          id={chat.id}
                          message={chat.text}
                          hasGrammarError={chat.hasGrammarError}
                          correctedSentence={chat.correctedSentence}
                          grammarFeedbackFa={chat.grammarFeedbackFa}
                          isEvaluatingGrammar={chat.isEvaluatingGrammar}
                          isSending={isSending && chat.id === chats[chats.length - 2]?.id}
                        />
                      </div>
                    )
                  )}

                  <div ref={messagesEndRef} />
                </div>
              )}
            </main>

            {/* Floating Apple Liquid Glass Bottom Dock (Anchored, guaranteed visible) */}
            {!isLoadingStage && stage && (
              <footer className="shrink-0 z-30 px-3.5 pb-[max(env(safe-area-inset-bottom),14px)] pt-2">
                <ChatBottomBar
                  onSendMessage={handleSendMessage}
                  onRequestHint={handleRequestHint}
                  isModelSpeaking={isModelSpeaking}
                  isSending={isSending}
                  isLoadingHint={isLoadingHint}
                  isRecording={speechRecognition.isListening}
                  isProcessingRecording={speechRecognition.isProcessing}
                  recordingAudioLevel={speechRecognition.audioLevel}
                  recordingDuration={speechRecognition.recordingDuration}
                  recordingTranscript={speechRecognition.transcript}
                  hintsUsedCount={hintsUsedCount}
                  onStartRecording={speechRecognition.startListening}
                  onStopRecording={speechRecognition.stopListening}
                  onCancelRecording={speechRecognition.cancelListening}
                  onClearTranscript={speechRecognition.resetTranscript}
                  recordingError={speechRecognition.error}
                />
              </footer>
            )}
          </section>

          {/* C. Left Companion (in RTL: Interaction Telemetry & Live Grammar Feed) */}
          <aside className="hidden lg:block shrink-0">
            <LiveTelemetryCompanion
              turnsCount={turnsCount}
              maxTurns={stage?.maxTurns || 10}
              hintsUsedCount={hintsUsedCount}
              grammarErrors={grammarErrorsCollected}
              onRequestHint={handleRequestHint}
            />
          </aside>
        </div>
      </div>

      {/* 3. Objective Modal */}
      <ObjectiveModal
        isOpen={isObjectiveOpen}
        onClose={() => setIsObjectiveOpen(false)}
        stage={stage}
      />

      {/* 4. Hint Suggestion Modal */}
      {hintData && (
        <HintModal
          isOpen={isHintOpen}
          onClose={() => setIsHintOpen(false)}
          hintsUsedCount={hintsUsedCount}
          suggestion={hintData.suggestion}
          explanationFa={hintData.explanationFa}
          onUseSuggestion={(text) => handleSendMessage(text)}
          onPlayAudio={(text) => audioPlayer.play('hint_audio', text)}
        />
      )}

      {/* 5. Evaluation Result Modal */}
      <EvaluationModal
        isOpen={isEvaluationOpen}
        onClose={() => setIsEvaluationOpen(false)}
        evaluation={evaluationData}
        stageTitle={stage?.titleFa}
        onNextStage={handleNextStage}
        onRetryStage={handleRetryStage}
        onBackToMap={() => navigate('/')}
      />

      {/* 6. Exit Confirmation Modal */}
      <ExitConfirmModal
        isOpen={isExitConfirmOpen}
        onClose={() => setIsExitConfirmOpen(false)}
        onConfirmExit={() => {
          setIsExitConfirmOpen(false);
          navigate('/');
        }}
      />

      {/* 7. Floating Apple Toast */}
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
