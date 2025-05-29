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
  createdAt: Date;
  updatedAt: Date;
};

export type Question = {
  id: string;
  text: string;
  answerOptions: Answer[];
  createdAt: Date;
  updatedAt: Date;
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
  count: number;
  percentage: number;
  correct?: boolean;
};
