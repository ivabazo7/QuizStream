import { Card, Collapse, List } from 'antd';
import { Quiz } from '../types/quiz';

const { Meta } = Card;

type Props = {
  quiz: Quiz;
};

function QuizDetailsCard({ quiz }: Props) {
  return (
    <Card title={quiz.name} style={{ margin: '20px auto', maxWidth: 800 }}>
      <Meta description={`Last update: ${new Date(quiz.updatedAt).toLocaleString()}`} />
      <br />
      <p>{quiz.description}</p>
      <br />
      <Collapse
        items={quiz.questions.map((question, index) => ({
          key: String(index),
          label: `Q${index + 1}: ${question.text}`,
          children: (
            <List
              bordered
              dataSource={question.answerOptions}
              renderItem={answer => (
                <List.Item
                  style={{
                    backgroundColor: answer.correct ? '#77ee77' : 'transparent',
                    borderRadius: '4px',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                  }}
                >
                  {answer.text}
                </List.Item>
              )}
            />
          ),
        }))}
      />
    </Card>
  );
}

export default QuizDetailsCard;
