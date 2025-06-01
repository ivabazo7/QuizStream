--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9 (Ubuntu 16.9-1.pgdg24.04+1)
-- Dumped by pg_dump version 17.5 (Ubuntu 17.5-1.pgdg24.04+1)

-- Started on 2025-06-01 18:03:30 CEST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
--SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 222 (class 1259 OID 346614)
-- Name: answer_option; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.answer_option (
    id bigint NOT NULL,
    question_id bigint NOT NULL,
    text character varying(255) NOT NULL,
    is_correct boolean NOT NULL
);


ALTER TABLE public.answer_option OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 346613)
-- Name: answer_option_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.answer_option_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.answer_option_id_seq OWNER TO postgres;

--
-- TOC entry 3499 (class 0 OID 0)
-- Dependencies: 221
-- Name: answer_option_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.answer_option_id_seq OWNED BY public.answer_option.id;


--
-- TOC entry 216 (class 1259 OID 346566)
-- Name: moderator; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.moderator (
    id bigint NOT NULL,
    username character varying(100) NOT NULL,
    email character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.moderator OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 346565)
-- Name: moderator_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.moderator_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.moderator_id_seq OWNER TO postgres;

--
-- TOC entry 3500 (class 0 OID 0)
-- Dependencies: 215
-- Name: moderator_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.moderator_id_seq OWNED BY public.moderator.id;


--
-- TOC entry 220 (class 1259 OID 346597)
-- Name: question; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.question (
    id bigint NOT NULL,
    quiz_id bigint NOT NULL,
    text character varying(255) NOT NULL
);


ALTER TABLE public.question OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 346596)
-- Name: question_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.question_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.question_id_seq OWNER TO postgres;

--
-- TOC entry 3501 (class 0 OID 0)
-- Dependencies: 219
-- Name: question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.question_id_seq OWNED BY public.question.id;


--
-- TOC entry 218 (class 1259 OID 346580)
-- Name: quiz; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.quiz (
    id bigint NOT NULL,
    moderator_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.quiz OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 346579)
-- Name: quiz_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.quiz_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.quiz_id_seq OWNER TO postgres;

--
-- TOC entry 3502 (class 0 OID 0)
-- Dependencies: 217
-- Name: quiz_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.quiz_id_seq OWNED BY public.quiz.id;


--
-- TOC entry 224 (class 1259 OID 346631)
-- Name: quiz_instance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.quiz_instance (
    id bigint NOT NULL,
    quiz_id bigint NOT NULL,
    code character varying(6) NOT NULL,
    start_timestamp timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    end_timestamp timestamp without time zone
);


ALTER TABLE public.quiz_instance OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 346630)
-- Name: quiz_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.quiz_instance_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.quiz_instance_id_seq OWNER TO postgres;

--
-- TOC entry 3503 (class 0 OID 0)
-- Dependencies: 223
-- Name: quiz_instance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.quiz_instance_id_seq OWNED BY public.quiz_instance.id;


--
-- TOC entry 226 (class 1259 OID 346646)
-- Name: quiz_response; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.quiz_response (
    id bigint NOT NULL,
    quiz_instance_id bigint NOT NULL,
    answer_option_id bigint NOT NULL,
    votes_count integer DEFAULT 0 NOT NULL,
    participant_ids jsonb DEFAULT '[]'::jsonb
);


ALTER TABLE public.quiz_response OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 346645)
-- Name: quiz_response_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.quiz_response_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.quiz_response_id_seq OWNER TO postgres;

--
-- TOC entry 3504 (class 0 OID 0)
-- Dependencies: 225
-- Name: quiz_response_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.quiz_response_id_seq OWNED BY public.quiz_response.id;


--
-- TOC entry 3314 (class 2604 OID 346668)
-- Name: answer_option id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_option ALTER COLUMN id SET DEFAULT nextval('public.answer_option_id_seq'::regclass);


--
-- TOC entry 3308 (class 2604 OID 346688)
-- Name: moderator id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderator ALTER COLUMN id SET DEFAULT nextval('public.moderator_id_seq'::regclass);


--
-- TOC entry 3313 (class 2604 OID 346704)
-- Name: question id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question ALTER COLUMN id SET DEFAULT nextval('public.question_id_seq'::regclass);


--
-- TOC entry 3310 (class 2604 OID 346724)
-- Name: quiz id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz ALTER COLUMN id SET DEFAULT nextval('public.quiz_id_seq'::regclass);


--
-- TOC entry 3315 (class 2604 OID 346749)
-- Name: quiz_instance id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_instance ALTER COLUMN id SET DEFAULT nextval('public.quiz_instance_id_seq'::regclass);


--
-- TOC entry 3317 (class 2604 OID 346770)
-- Name: quiz_response id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_response ALTER COLUMN id SET DEFAULT nextval('public.quiz_response_id_seq'::regclass);


--
-- TOC entry 3335 (class 2606 OID 346670)
-- Name: answer_option answer_option_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_option
    ADD CONSTRAINT answer_option_pkey PRIMARY KEY (id);


--
-- TOC entry 3323 (class 2606 OID 346576)
-- Name: moderator moderator_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderator
    ADD CONSTRAINT moderator_email_key UNIQUE (email);


--
-- TOC entry 3325 (class 2606 OID 346690)
-- Name: moderator moderator_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderator
    ADD CONSTRAINT moderator_pkey PRIMARY KEY (id);


--
-- TOC entry 3327 (class 2606 OID 347322)
-- Name: moderator moderator_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.moderator
    ADD CONSTRAINT moderator_username_key UNIQUE (username);


--
-- TOC entry 3333 (class 2606 OID 346706)
-- Name: question question_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question
    ADD CONSTRAINT question_pkey PRIMARY KEY (id);


--
-- TOC entry 3340 (class 2606 OID 346751)
-- Name: quiz_instance quiz_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_instance
    ADD CONSTRAINT quiz_instance_pkey PRIMARY KEY (id);


--
-- TOC entry 3330 (class 2606 OID 346726)
-- Name: quiz quiz_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz
    ADD CONSTRAINT quiz_pkey PRIMARY KEY (id);


--
-- TOC entry 3344 (class 2606 OID 346772)
-- Name: quiz_response quiz_response_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_response
    ADD CONSTRAINT quiz_response_pkey PRIMARY KEY (id);


--
-- TOC entry 3336 (class 1259 OID 346629)
-- Name: idx_answer_option_question_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_answer_option_question_id ON public.answer_option USING btree (question_id);


--
-- TOC entry 3320 (class 1259 OID 346578)
-- Name: idx_moderator_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_moderator_email ON public.moderator USING btree (email);


--
-- TOC entry 3321 (class 1259 OID 347323)
-- Name: idx_moderator_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_moderator_username ON public.moderator USING btree (username);


--
-- TOC entry 3331 (class 1259 OID 346612)
-- Name: idx_question_quiz_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_question_quiz_id ON public.question USING btree (quiz_id);


--
-- TOC entry 3337 (class 1259 OID 346763)
-- Name: idx_quiz_instance_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quiz_instance_code ON public.quiz_instance USING btree (code);


--
-- TOC entry 3338 (class 1259 OID 346644)
-- Name: idx_quiz_instance_quiz_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quiz_instance_quiz_id ON public.quiz_instance USING btree (quiz_id);


--
-- TOC entry 3328 (class 1259 OID 346595)
-- Name: idx_quiz_moderator_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quiz_moderator_id ON public.quiz USING btree (moderator_id);


--
-- TOC entry 3341 (class 1259 OID 346667)
-- Name: idx_quiz_response_answer_option_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quiz_response_answer_option_id ON public.quiz_response USING btree (answer_option_id);


--
-- TOC entry 3342 (class 1259 OID 346666)
-- Name: idx_quiz_response_quiz_instance_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quiz_response_quiz_instance_id ON public.quiz_response USING btree (quiz_instance_id);


--
-- TOC entry 3347 (class 2606 OID 346707)
-- Name: answer_option answer_option_question_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.answer_option
    ADD CONSTRAINT answer_option_question_id_fkey FOREIGN KEY (question_id) REFERENCES public.question(id);


--
-- TOC entry 3346 (class 2606 OID 346727)
-- Name: question question_quiz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.question
    ADD CONSTRAINT question_quiz_id_fkey FOREIGN KEY (quiz_id) REFERENCES public.quiz(id);


--
-- TOC entry 3348 (class 2606 OID 346732)
-- Name: quiz_instance quiz_instance_quiz_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_instance
    ADD CONSTRAINT quiz_instance_quiz_id_fkey FOREIGN KEY (quiz_id) REFERENCES public.quiz(id);


--
-- TOC entry 3345 (class 2606 OID 346691)
-- Name: quiz quiz_moderator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz
    ADD CONSTRAINT quiz_moderator_id_fkey FOREIGN KEY (moderator_id) REFERENCES public.moderator(id);


--
-- TOC entry 3349 (class 2606 OID 346671)
-- Name: quiz_response quiz_response_answer_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_response
    ADD CONSTRAINT quiz_response_answer_option_id_fkey FOREIGN KEY (answer_option_id) REFERENCES public.answer_option(id);


--
-- TOC entry 3350 (class 2606 OID 346752)
-- Name: quiz_response quiz_response_quiz_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quiz_response
    ADD CONSTRAINT quiz_response_quiz_instance_id_fkey FOREIGN KEY (quiz_instance_id) REFERENCES public.quiz_instance(id);


-- Completed on 2025-06-01 18:03:30 CEST

--
-- PostgreSQL database dump complete
--

