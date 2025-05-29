CREATE TABLE moderator (
 	id SERIAL PRIMARY KEY,
 	username VARCHAR(20) NOT NULL UNIQUE,
 	email VARCHAR(50) NOT NULL UNIQUE,
 	password VARCHAR(255) NOT NULL,
 	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
 CREATE INDEX idx_moderator_username ON moderator(username);
 CREATE INDEX idx_moderator_email ON moderator(email);

 CREATE TABLE quiz (
 	id SERIAL PRIMARY KEY,
 	moderator_id BIGINT NOT NULL REFERENCES moderator(id),
 	name VARCHAR(100) NOT NULL,
 	description TEXT,
 	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
 CREATE INDEX idx_quiz_moderator_id ON quiz(moderator_id);

 CREATE TABLE question (
 	id SERIAL PRIMARY KEY,
 	quiz_id BIGINT NOT NULL REFERENCES quiz(id),
 	text TEXT NOT NULL,
 	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
 CREATE INDEX idx_question_quiz_id ON question(quiz_id);

 CREATE TABLE answer_option (
 	id SERIAL PRIMARY KEY,
 	question_id BIGINT NOT NULL REFERENCES question(id),
 	text TEXT NOT NULL,
 	is_correct BOOLEAN NOT NULL,
 	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 );
 CREATE INDEX idx_answer_option_question_id ON answer_option(question_id);

 CREATE TABLE quiz_instance (
 	id SERIAL PRIMARY KEY,
 	quiz_id BIGINT NOT NULL REFERENCES quiz(id),
 	code CHAR(6) NOT NULL,
 	start_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 	end_timestamp TIMESTAMP
 );
 CREATE INDEX idx_quiz_instance_code ON quiz_instance(code);
 CREATE INDEX idx_quiz_instance_quiz_id ON quiz_instance(quiz_id);

 CREATE TABLE quiz_response (
 	id SERIAL PRIMARY KEY,
 	quiz_instance_id BIGINT NOT NULL REFERENCES quiz_instance(id),
 	answer_option_id BIGINT NOT NULL REFERENCES answer_option(id),
 	votes_count INTEGER NOT NULL DEFAULT 0,
 	participant_ids JSONB DEFAULT '[]'::JSONB -- pohrana UUID-ova sudionika
 );
 CREATE INDEX idx_quiz_response_quiz_instance_id ON quiz_response(quiz_instance_id);
 CREATE INDEX idx_quiz_response_answer_option_id ON quiz_response(answer_option_id);