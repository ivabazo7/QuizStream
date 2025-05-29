import { Button, Form, Input, Card } from 'antd';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { username: string; email: string; password: string }) => {
    await register(values.username, values.email, values.password);
    navigate('/');
  };

  return (
    <Card title="Registracija" style={{ width: 400, margin: '100px auto' }}>
      <Form onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Korisničko ime"
          name="username"
          rules={[{ required: true, message: 'Unesite korisničko ime' }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label="Email"
          name="email"
          rules={[{ required: true, type: 'email', message: 'Unesite valjani email' }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label="Lozinka"
          name="password"
          rules={[{ required: true, message: 'Unesite lozinku' }]}
        >
          <Input.Password />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>
            Registriraj se
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
}

export default RegisterPage;
