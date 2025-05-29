import { Route, Routes } from 'react-router-dom';
import HomePage from './pages/HomePage';
import QuizParticipantPage from './pages/QuizParticipantPage';
import QuizModeratorPage from './pages/QuizModeratorPage';
import PageLayout from './pages/PageLayout';
import QuizDetailsPage from './pages/QuizDetailsPage';
import ProtectedRoute from './components/ProtectedRoute';
import RegisterPage from './pages/RegisterPage';
import LoginPage from './pages/LoginPage';
import { AuthProvider } from './contexts/AuthContext';

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<PageLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/quiz/:quizCode" element={<QuizParticipantPage />} />

          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/:moderatorId/quiz/"
            element={
              <ProtectedRoute>
                <QuizModeratorPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/:moderatorId/quiz/:quizId"
            element={
              <ProtectedRoute>
                <QuizDetailsPage />
              </ProtectedRoute>
            }
          />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
