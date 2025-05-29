import { Card, List } from 'antd';
import { ParticipantAnswers, Question } from '../types/quiz';

type Props = {
  question: Question;
  participantAnswer: ParticipantAnswers | null;
  handleAnswerClick(answerId: string): void;
};

function QuestionAnswerCard({ question, participantAnswer, handleAnswerClick }: Props) {
  return (
    <Card key={question.id} type="inner" title={question.text} style={{ marginTop: 16 }}>
      <List
        bordered
        dataSource={question.answerOptions}
        renderItem={answer => (
          <List.Item
            style={{
              backgroundColor: participantAnswer?.answerIds.includes(answer.id)
                ? '#bbbbbb'
                : 'transparent',
              borderRadius: '4px',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              textAlign: 'center',
              cursor: 'pointer',
              transition: '0.2s',
            }}
            onClick={() => handleAnswerClick(answer.id)}
            onMouseEnter={e => {
              if (!participantAnswer?.answerIds.includes(answer.id)) {
                e.currentTarget.style.backgroundColor = '#f0f0f0';
              }
            }}
            onMouseLeave={e => {
              if (!participantAnswer?.answerIds.includes(answer.id)) {
                e.currentTarget.style.backgroundColor = 'transparent';
              }
            }}
          >
            {answer.text}
          </List.Item>
        )}
      />
    </Card>
  );
}

export default QuestionAnswerCard;
