export type AnswerEdit = {
  text: string;
  correct: boolean;
};

export type QuestionEdit = {
  text: string;
  answerOptions: AnswerEdit[];
};

export type QuizEdit = {
  moderatorId?: number;
  name: string;
  description?: string;
  questions: QuestionEdit[];
};

export type Answer = {
  id: string;
  text: string;
  correct: boolean;
};

export type Question = {
  id: string;
  text: string;
  answerOptions: Answer[];
};

export type Quiz = {
  id: number;
  name: string;
  description?: string;
  questions: Question[];
  createdAt: Date;
  updatedAt: Date;
};

export type ParticipantAnswers = {
  questionId: string;
  answerIds: string[];
};

export type AnswerResultStat = {
  answerId: string;
  correct?: boolean;
  count: number;
  percentage: number;
};

export type ModeratorState = {
  currentQuestionIndex: number;
  participantCount: number;
  showResults: boolean;
  resultsStat: AnswerResultStat[] | null;
};

export type ParticipantState = {
  currentQuestion: Question;
  showResults: boolean;
  resultsStat: AnswerResultStat[] | null;
};
