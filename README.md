# DocAI: Full-Stack AI App (Spring Boot, MySQL, RAG, LLMs)

Upload documents, then chat with them. DocAI uses Retrieval-Augmented Generation (RAG) to answer from your files and cite the passages it used. It also offers model-only chat, document summarisation, semantic search and zero-shot text classification.

**Stack:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, MySQL 8, Apache PDFBox, Ollama (local LLM and embeddings) or OpenAI, vanilla JavaScript front end.

## Features

| Feature | AI technique |
|---------|--------------|
| Chat with documents | RAG: chunking, embeddings, cosine-similarity retrieval, grounded generation with citations |
| Chat with the model only | LLM chat with conversation memory (last 6 messages) |
| Summarise a document | LLM summarisation |
| Semantic search | Embedding-based similarity search, no LLM call |
| Text classifier | Zero-shot classification with an LLM |
| Accounts | Registration and login with BCrypt and JWT; every user only sees their own data |

## Architecture

```
Browser (static HTML/JS)
   |  REST + JWT
Spring Boot  --  Controllers -> Services -> Repositories -> MySQL 8
   |                |
   |                +-- RAG: TextChunker, DocumentTextExtractor, RetrievalService
   |
   +-- LlmClient (interface)
          +-- OllamaClient  (default, local and free)
          +-- OpenAiClient  (ai.provider=openai)
```

RAG flow:
1. **Ingest:** PDF/TXT/MD text is extracted page by page, split into overlapping chunks, embedded in batches and stored in MySQL (`chunks` table, vector as BLOB).
2. **Retrieve:** the question is embedded, scored against the user's chunks by cosine similarity, and the top-k are selected.
3. **Generate:** a prompt with the numbered excerpts and recent chat history goes to the LLM, which is told to answer only from the excerpts and cite them like [1].
4. **Store:** the question, answer and sources are saved in `chat_messages`.

## Prerequisites

1. JDK 17 or newer
2. Spring Tool Suite 4 (STS), with its built-in Maven support
3. MySQL 8 running on port 3306 (or `docker compose up -d` using the included file)
4. [Ollama](https://ollama.com) with two models:
   ```
   ollama pull llama3.2
   ollama pull nomic-embed-text
   ```

## Run in Spring Tool Suite

1. **File > Import > Maven > Existing Maven Projects**, select this folder, then Finish. Wait for dependencies to download.
2. Check the database login in `src/main/resources/application.properties` (default user `root`, password `root`). Change it, or set the environment variables `DB_USER` and `DB_PASSWORD` in the run configuration. The `docai` database is created automatically.
3. Right-click `DocAiApplication.java > Run As > Spring Boot App`.
4. Open http://localhost:8080, create an account, upload a document and ask a question.

Command line alternative: `mvn spring-boot:run`

## Use OpenAI instead of Ollama

Set these environment variables (STS: Run Configurations > Environment):

```
AI_PROVIDER=openai
OPENAI_API_KEY=your-key
```

If you switch embedding models, delete and re-upload your documents. Vectors from different models cannot be compared.

## REST API

All endpoints except `/api/auth/**` need the header `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/register`, `/api/auth/login` | Returns `{token, email}` |
| GET | `/api/documents` | List documents |
| POST | `/api/documents` | Upload (multipart field `file`) |
| POST | `/api/documents/{id}/summarize` | Generate a summary |
| DELETE | `/api/documents/{id}` | Delete document and chunks |
| POST | `/api/chat/ask` | `{question, sessionId?, mode: "RAG" or "LLM", documentId?}` |
| GET | `/api/chat/sessions` | List chats |
| GET | `/api/chat/sessions/{id}/messages` | Chat history with sources |
| POST | `/api/ai/search` | `{query, documentId?}` semantic search |
| POST | `/api/ai/classify` | `{text, labels: [...]}` |

## Tests

`mvn test` runs unit tests for the chunking and vector-math code.

## Design notes and limits

- Vectors are stored in MySQL and scored in memory. This is simple and fine for thousands of chunks. For millions, replace `RetrievalService` with a vector database (pgvector, Qdrant, Milvus).
- Embedding is done synchronously during upload, so large PDFs take time on a CPU-only machine.
- Scanned PDFs have no text layer and need OCR first.
- Document text is treated as untrusted in the prompt to reduce prompt-injection risk.
- Before deploying: set a strong `JWT_SECRET`, use a dedicated MySQL user, serve over HTTPS, and add rate limiting.
- Ideas to extend: streaming answers (SSE), re-ranking, hybrid keyword + vector search, an evaluation set for retrieval quality, and a React front end.
