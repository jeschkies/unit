--
-- PostgreSQL database dump
--

-- Dumped from database version 10.5
-- Dumped by pg_dump version 10.6 (Ubuntu 10.6-0ubuntu0.18.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: kjeschkies
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO kjeschkies;

--
-- Name: organizations; Type: TABLE; Schema: public; Owner: kjeschkies
--

CREATE TABLE public.organizations (
    organization_id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.organizations OWNER TO kjeschkies;

--
-- Name: organizations_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: kjeschkies
--

CREATE SEQUENCE public.organizations_organization_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organizations_organization_id_seq OWNER TO kjeschkies;

--
-- Name: organizations_organization_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kjeschkies
--

ALTER SEQUENCE public.organizations_organization_id_seq OWNED BY public.organizations.organization_id;


--
-- Name: reports; Type: TABLE; Schema: public; Owner: kjeschkies
--

CREATE TABLE public.reports (
    report_id integer NOT NULL,
    organization_id integer,
    repository_id integer,
    commit_hash text NOT NULL,
    prefix text NOT NULL
);


ALTER TABLE public.reports OWNER TO kjeschkies;

--
-- Name: reports_report_id_seq; Type: SEQUENCE; Schema: public; Owner: kjeschkies
--

CREATE SEQUENCE public.reports_report_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reports_report_id_seq OWNER TO kjeschkies;

--
-- Name: reports_report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kjeschkies
--

ALTER SEQUENCE public.reports_report_id_seq OWNED BY public.reports.report_id;


--
-- Name: repositories; Type: TABLE; Schema: public; Owner: kjeschkies
--

CREATE TABLE public.repositories (
    repository_id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.repositories OWNER TO kjeschkies;

--
-- Name: repositories_repository_id_seq; Type: SEQUENCE; Schema: public; Owner: kjeschkies
--

CREATE SEQUENCE public.repositories_repository_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.repositories_repository_id_seq OWNER TO kjeschkies;

--
-- Name: repositories_repository_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kjeschkies
--

ALTER SEQUENCE public.repositories_repository_id_seq OWNED BY public.repositories.repository_id;


--
-- Name: testsuites; Type: TABLE; Schema: public; Owner: kjeschkies
--

CREATE TABLE public.testsuites (
    testsuite_id integer NOT NULL,
    report_id integer,
    filename text,
    payload xml NOT NULL
);


ALTER TABLE public.testsuites OWNER TO kjeschkies;

--
-- Name: testsuites_testsuite_id_seq; Type: SEQUENCE; Schema: public; Owner: kjeschkies
--

CREATE SEQUENCE public.testsuites_testsuite_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.testsuites_testsuite_id_seq OWNER TO kjeschkies;

--
-- Name: testsuites_testsuite_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kjeschkies
--

ALTER SEQUENCE public.testsuites_testsuite_id_seq OWNED BY public.testsuites.testsuite_id;


--
-- Name: organizations organization_id; Type: DEFAULT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.organizations ALTER COLUMN organization_id SET DEFAULT nextval('public.organizations_organization_id_seq'::regclass);


--
-- Name: reports report_id; Type: DEFAULT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.reports ALTER COLUMN report_id SET DEFAULT nextval('public.reports_report_id_seq'::regclass);


--
-- Name: repositories repository_id; Type: DEFAULT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.repositories ALTER COLUMN repository_id SET DEFAULT nextval('public.repositories_repository_id_seq'::regclass);


--
-- Name: testsuites testsuite_id; Type: DEFAULT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.testsuites ALTER COLUMN testsuite_id SET DEFAULT nextval('public.testsuites_testsuite_id_seq'::regclass);


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: kjeschkies
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	0	Boostraph	SQL	V0__Boostraph.sql	854922517	kjeschkies	2018-12-30 19:55:11.060933	47	t
\.


--
-- Data for Name: organizations; Type: TABLE DATA; Schema: public; Owner: kjeschkies
--

COPY public.organizations (organization_id, name) FROM stdin;
1	jeschkies
\.


--
-- Data for Name: reports; Type: TABLE DATA; Schema: public; Owner: kjeschkies
--

COPY public.reports (report_id, organization_id, repository_id, commit_hash, prefix) FROM stdin;
1	1	1	deafbeef	system-test
2	1	1	deafbeef	system-test
3	1	1	deafbeef	system-test
4	1	1	deafbeef	system-test
5	1	1	deafbeef	system-test
6	1	1	deafbeef	system-test
7	1	1	deafbeef	system-test
\.


--
-- Data for Name: repositories; Type: TABLE DATA; Schema: public; Owner: kjeschkies
--

COPY public.repositories (repository_id, name) FROM stdin;
1	unit
\.


--
-- Data for Name: testsuites; Type: TABLE DATA; Schema: public; Owner: kjeschkies
--

COPY public.testsuites (testsuite_id, report_id, filename, payload) FROM stdin;
1	1	deadbeef.xml	<testsuite errors="0" failures="1" name="pytest" skipped="0" tests="2" time="0.224"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.052658796310424805"><failure message="AssertionError: assert 200 == 201\n +  where 200 = &lt;ClientResponse(http://127.0.0.1:54359/) [200 OK]&gt;\\n&lt;CIMultiDictProxy(&apos;Content-Type&apos;: &apos;text/html; charset=utf-8&apos;, &apos;Content-Length&apos;: &apos;480&apos;, &apos;Date&apos;: &apos;Mon, 18 Dec 2017 20:21:38 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x107342588&gt;\n\n    async def test_index(cli):\n        response = await cli.get(&apos;/&apos;)\n&gt;       assert response.status == 201\nE       AssertionError: assert 200 == 201\nE        +  where 200 = &lt;ClientResponse(http://127.0.0.1:54359/) [200 OK]&gt;\\n&lt;CIMultiDictProxy(&apos;Content-Type&apos;: &apos;text/html; charset=utf-8&apos;, &apos;Content-Length&apos;: &apos;480&apos;, &apos;Date&apos;: &apos;Mon, 18 Dec 2017 20:21:38 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;)&gt;\\n.status\n\ntests/test_app.py:13: AssertionError</failure></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.022773265838623047"></testcase></testsuite>
2	1	exception.xml	<testsuite errors="0" failures="1" name="pytest" tests="3" time="0.328"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase><testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"><failure message="AssertionError: assert 500 == 404\n +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x10ffdd4a8&gt;\n\n    async def test_junit_not_found(cli):\n        response = await cli.get(&apos;/jeschkies/unit/commit/unknown&apos;)\n&gt;       assert response.status == 404\nE       AssertionError: assert 500 == 404\nE        +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status\n\ntests/test_app.py:23: AssertionError</failure><system-err>web_protocol.py            352 ERROR    Error handling request\nTraceback (most recent call last):\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_protocol.py&quot;, line 410, in start\n    resp = yield from self._request_handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web.py&quot;, line 325, in _handle\n    resp = yield from handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_middlewares.py&quot;, line 93, in impl\n    return (yield from handler(request))\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp_jinja2/__init__.py&quot;, line 88, in wrapped\n    context = yield from coro(*args)\n  File &quot;/Users/karsten/Documents/unit/fm.unit.unitfm/main.py&quot;, line 41, in view_junit\n    raw_junit = request.app[&apos;junits&apos;][commit_sha]\nKeyError: &apos;unknown&apos;\n</system-err></testcase></testsuite>\n
3	2	pass.xml	<testsuite errors="0" failures="0" name="pytest" tests="3" time="0.328">\n\t<testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase>\n\t<testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase>\n\t<testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"></testcase>\n</testsuite>\n
4	3	pass.xml	<testsuite errors="0" failures="0" name="pytest" tests="3" time="0.328">\n\t<testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase>\n\t<testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase>\n\t<testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"></testcase>\n</testsuite>\n
5	4	pass.xml	<testsuite errors="0" failures="0" name="pytest" tests="3" time="0.328">\n\t<testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase>\n\t<testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase>\n\t<testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"></testcase>\n</testsuite>\n
6	5	exception.xml	<testsuite errors="0" failures="1" name="pytest" tests="3" time="0.328"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase><testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"><failure message="AssertionError: assert 500 == 404\n +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x10ffdd4a8&gt;\n\n    async def test_junit_not_found(cli):\n        response = await cli.get(&apos;/jeschkies/unit/commit/unknown&apos;)\n&gt;       assert response.status == 404\nE       AssertionError: assert 500 == 404\nE        +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status\n\ntests/test_app.py:23: AssertionError</failure><system-err>web_protocol.py            352 ERROR    Error handling request\nTraceback (most recent call last):\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_protocol.py&quot;, line 410, in start\n    resp = yield from self._request_handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web.py&quot;, line 325, in _handle\n    resp = yield from handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_middlewares.py&quot;, line 93, in impl\n    return (yield from handler(request))\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp_jinja2/__init__.py&quot;, line 88, in wrapped\n    context = yield from coro(*args)\n  File &quot;/Users/karsten/Documents/unit/fm.unit.unitfm/main.py&quot;, line 41, in view_junit\n    raw_junit = request.app[&apos;junits&apos;][commit_sha]\nKeyError: &apos;unknown&apos;\n</system-err></testcase></testsuite>\n
7	6	exception.xml	<testsuite errors="0" failures="1" name="pytest" tests="3" time="0.328"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase><testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"><failure message="AssertionError: assert 500 == 404\n +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x10ffdd4a8&gt;\n\n    async def test_junit_not_found(cli):\n        response = await cli.get(&apos;/jeschkies/unit/commit/unknown&apos;)\n&gt;       assert response.status == 404\nE       AssertionError: assert 500 == 404\nE        +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status\n\ntests/test_app.py:23: AssertionError</failure><system-err>web_protocol.py            352 ERROR    Error handling request\nTraceback (most recent call last):\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_protocol.py&quot;, line 410, in start\n    resp = yield from self._request_handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web.py&quot;, line 325, in _handle\n    resp = yield from handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_middlewares.py&quot;, line 93, in impl\n    return (yield from handler(request))\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp_jinja2/__init__.py&quot;, line 88, in wrapped\n    context = yield from coro(*args)\n  File &quot;/Users/karsten/Documents/unit/fm.unit.unitfm/main.py&quot;, line 41, in view_junit\n    raw_junit = request.app[&apos;junits&apos;][commit_sha]\nKeyError: &apos;unknown&apos;\n</system-err></testcase></testsuite>\n
8	7	deadbeef.xml	<testsuite errors="0" failures="1" name="pytest" skipped="0" tests="2" time="0.224"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.052658796310424805"><failure message="AssertionError: assert 200 == 201\n +  where 200 = &lt;ClientResponse(http://127.0.0.1:54359/) [200 OK]&gt;\\n&lt;CIMultiDictProxy(&apos;Content-Type&apos;: &apos;text/html; charset=utf-8&apos;, &apos;Content-Length&apos;: &apos;480&apos;, &apos;Date&apos;: &apos;Mon, 18 Dec 2017 20:21:38 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x107342588&gt;\n\n    async def test_index(cli):\n        response = await cli.get(&apos;/&apos;)\n&gt;       assert response.status == 201\nE       AssertionError: assert 200 == 201\nE        +  where 200 = &lt;ClientResponse(http://127.0.0.1:54359/) [200 OK]&gt;\\n&lt;CIMultiDictProxy(&apos;Content-Type&apos;: &apos;text/html; charset=utf-8&apos;, &apos;Content-Length&apos;: &apos;480&apos;, &apos;Date&apos;: &apos;Mon, 18 Dec 2017 20:21:38 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;)&gt;\\n.status\n\ntests/test_app.py:13: AssertionError</failure></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.022773265838623047"></testcase></testsuite>
9	7	exception.xml	<testsuite errors="0" failures="1" name="pytest" tests="3" time="0.328"><testcase classname="tests.test_app" name="test_index[pyloop]" time="0.06121206283569336"></testcase><testcase classname="tests.test_app" name="test_rejection[pyloop]" time="0.023204565048217773"></testcase><testcase classname="tests.test_app" name="test_junit_not_found[pyloop]" time="0.03633594512939453"><failure message="AssertionError: assert 500 == 404\n +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status">cli = &lt;aiohttp.test_utils.TestClient object at 0x10ffdd4a8&gt;\n\n    async def test_junit_not_found(cli):\n        response = await cli.get(&apos;/jeschkies/unit/commit/unknown&apos;)\n&gt;       assert response.status == 404\nE       AssertionError: assert 500 == 404\nE        +  where 500 = &lt;ClientResponse(http://127.0.0.1:57329/jeschkies/unit/commit/unknown) [500 Internal Server Error]&gt;\\n&lt;CIMultiDictProxy(&apos;...Length&apos;: &apos;141&apos;, &apos;Date&apos;: &apos;Tue, 19 Dec 2017 18:49:51 GMT&apos;, &apos;Server&apos;: &apos;Python/3.6 aiohttp/2.3.6&apos;, &apos;Connection&apos;: &apos;close&apos;)&gt;\\n.status\n\ntests/test_app.py:23: AssertionError</failure><system-err>web_protocol.py            352 ERROR    Error handling request\nTraceback (most recent call last):\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_protocol.py&quot;, line 410, in start\n    resp = yield from self._request_handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web.py&quot;, line 325, in _handle\n    resp = yield from handler(request)\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp/web_middlewares.py&quot;, line 93, in impl\n    return (yield from handler(request))\n  File &quot;/Users/karsten/.local/share/virtualenvs/unit-fFY_xGcI/lib/python3.6/site-packages/aiohttp_jinja2/__init__.py&quot;, line 88, in wrapped\n    context = yield from coro(*args)\n  File &quot;/Users/karsten/Documents/unit/fm.unit.unitfm/main.py&quot;, line 41, in view_junit\n    raw_junit = request.app[&apos;junits&apos;][commit_sha]\nKeyError: &apos;unknown&apos;\n</system-err></testcase></testsuite>\n
\.


--
-- Name: organizations_organization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kjeschkies
--

SELECT pg_catalog.setval('public.organizations_organization_id_seq', 1, true);


--
-- Name: reports_report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kjeschkies
--

SELECT pg_catalog.setval('public.reports_report_id_seq', 7, true);


--
-- Name: repositories_repository_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kjeschkies
--

SELECT pg_catalog.setval('public.repositories_repository_id_seq', 1, true);


--
-- Name: testsuites_testsuite_id_seq; Type: SEQUENCE SET; Schema: public; Owner: kjeschkies
--

SELECT pg_catalog.setval('public.testsuites_testsuite_id_seq', 9, true);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: organizations organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (organization_id);


--
-- Name: reports reports_pkey; Type: CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (report_id);


--
-- Name: repositories repositories_pkey; Type: CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.repositories
    ADD CONSTRAINT repositories_pkey PRIMARY KEY (repository_id);


--
-- Name: testsuites testsuites_pkey; Type: CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.testsuites
    ADD CONSTRAINT testsuites_pkey PRIMARY KEY (testsuite_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: kjeschkies
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: reports reports_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(organization_id);


--
-- Name: reports reports_repository_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_repository_id_fkey FOREIGN KEY (repository_id) REFERENCES public.repositories(repository_id);


--
-- Name: testsuites testsuites_report_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kjeschkies
--

ALTER TABLE ONLY public.testsuites
    ADD CONSTRAINT testsuites_report_id_fkey FOREIGN KEY (report_id) REFERENCES public.reports(report_id);


--
-- PostgreSQL database dump complete
--

