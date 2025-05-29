import { CloseOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, Form, Input, Space } from 'antd';
import { AnswerEdit, QuestionEdit, Quiz, QuizEdit } from '../types/quiz';
import GradientButton from './ui/GradientButton';
import { useParams } from 'react-router-dom';
import { useEffect } from 'react';

type QuizCreateEditFormProps = {
  existingQuiz?: Quiz;
  onQuizCreated: (quiz: Quiz) => void;
};

function QuizCreateEditForm({ existingQuiz, onQuizCreated }: QuizCreateEditFormProps) {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const [form] = Form.useForm();
  const { moderatorId } = useParams();

  useEffect(() => {
    if (existingQuiz) {
      form.setFieldsValue(existingQuiz);
    }
  }, [existingQuiz, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();

      const payload: QuizEdit = {
        moderatorId: Number(moderatorId),
        name: values.name,
        description: values.description,
        questions: values.questions.map((q: QuestionEdit) => ({
          text: q.text,
          answerOptions: q.answerOptions.map((a: AnswerEdit) => ({
            text: a.text,
            correct: a.correct,
          })),
        })),
      };

      const url = existingQuiz ? `${API_BASE_URL}/quiz/${existingQuiz.id}` : `${API_BASE_URL}/quiz`;

      const method = existingQuiz ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error('Failed to save quiz');

      const savedQuiz = await response.json();
      onQuizCreated(savedQuiz as Quiz);
    } catch (error) {
      console.error('Validation failed:', error);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '1rem' }}>
      <Form
        layout="vertical"
        labelCol={{ span: 36 }}
        wrapperCol={{ span: 36 }}
        form={form}
        name="dynamic_form_complex"
        style={{ width: '50%', maxWidth: 600 }}
        autoComplete="off"
        initialValues={{ questions: [{}] }}
      >
        <Form.Item label="Name" name={['name']}>
          <Input.TextArea autoSize={{ minRows: 1, maxRows: 6 }} />
        </Form.Item>
        <Form.Item label="Description" name={['description']}>
          <Input.TextArea autoSize={{ minRows: 1, maxRows: 6 }} />
        </Form.Item>

        <Form.List name="questions">
          {(fields, { add, remove }) => (
            <div style={{ display: 'flex', rowGap: 16, flexDirection: 'column' }}>
              {fields.map(field => (
                <Card
                  size="small"
                  title={`Question ${field.name + 1}`}
                  key={field.key}
                  extra={
                    <CloseOutlined
                      onClick={() => {
                        remove(field.name);
                      }}
                    />
                  }
                >
                  <Form.Item label="Question" name={[field.name, 'text']}>
                    <Input.TextArea autoSize={{ minRows: 1, maxRows: 6 }} />
                  </Form.Item>

                  {/* Nest Form.List */}
                  <Form.Item label="Answer Options" style={{ margin: '1.5rem' }}>
                    <Form.List name={[field.name, 'answerOptions']}>
                      {(subFields, subOpt) => (
                        <div style={{ display: 'grid', gap: '16px' }}>
                          {subFields.map(subField => (
                            <Space
                              key={subField.key}
                              style={{ display: 'grid', gridTemplateColumns: '1fr auto auto' }}
                            >
                              <Form.Item noStyle name={[subField.name, 'text']}>
                                <Input.TextArea autoSize={{ minRows: 1, maxRows: 6 }} />
                              </Form.Item>
                              <Form.Item
                                noStyle
                                name={[subField.name, 'correct']}
                                valuePropName="checked"
                              >
                                <Checkbox />
                              </Form.Item>
                              <CloseOutlined
                                onClick={() => {
                                  subOpt.remove(subField.name);
                                }}
                              />
                            </Space>
                          ))}
                          <Button type="dashed" onClick={() => subOpt.add()} block>
                            + Add Answer Option
                          </Button>
                        </div>
                      )}
                    </Form.List>
                  </Form.Item>
                </Card>
              ))}

              <Button type="dashed" onClick={() => add()} block>
                + Add Question
              </Button>
            </div>
          )}
        </Form.List>
        <GradientButton style={{ margin: 20 }} text="Save" onClick={handleSave} />
      </Form>
    </div>
  );
}

export default QuizCreateEditForm;
