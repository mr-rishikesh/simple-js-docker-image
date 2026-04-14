# Frontend & API

A FastAPI backend exposes the ingestion pipeline and Qdrant-backed search to a
React (Vite) single-page app. The full product has three screens:

- **Search** — user-facing query UI with loading state and ranked results
- **Upload** — drag-a-file, live upload progress, then live pipeline progress
- **Admin** — indexed video list with delete, plus active ingestion jobs

---

## Architecture

```
┌──────────────┐    HTTP / SSE    ┌────────────────┐     ┌──────────┐
│  React app   │ ───────────────► │ FastAPI server │ ──► │  Qdrant  │
│ (Vite :5173) │ ◄─────────────── │  (:8000)       │     └──────────┘
└──────────────┘                  │                │           ▲
                                  │   utils/*      │ ──────────┘
                                  │   (pipeline)   │
                                  └────────────────┘
```

- Vite dev server proxies `/api/*` to `http://localhost:8000`, so the frontend
  uses same-origin URLs and there are no CORS concerns in dev.
- Ingestion runs in a background thread inside the FastAPI process. Each
  upload gets a `job_id`; the frontend subscribes to progress via
  Server-Sent Events (`/api/jobs/{id}/events`).
- Jobs live in-memory (see [server/jobs.py](server/jobs.py)). If you restart the
  server, in-flight jobs are lost — indexed videos survive because Qdrant is
  the durable store.

---

## API surface

| Method | Path                          | Purpose                                           |
| -----: | :---------------------------- | :------------------------------------------------ |
| `POST` | `/api/upload`                 | Upload a video file → returns `{ job_id }`        |
| `GET`  | `/api/jobs/{id}`              | Poll a single job's current state                 |
| `GET`  | `/api/jobs/{id}/events`       | SSE stream of progress updates                    |
| `GET`  | `/api/jobs`                   | List recent jobs (for Admin active-jobs panel)    |
| `GET`  | `/api/videos`                 | List indexed videos aggregated from Qdrant        |
| `DELETE` | `/api/videos/{video_id}`    | Remove a video and all its chunks from Qdrant     |
| `POST` | `/api/query`                  | Run a semantic query, returns ranked results      |
| `GET`  | `/api/health`                 | Liveness                                          |

### Upload
`POST /api/upload?reindex=false` with multipart `file=...`. Server writes the
upload to `data/uploads/`, then kicks off the pipeline in a background thread.

### Progress (SSE)
`GET /api/jobs/{id}/events` streams JSON payloads of the form:
```json
{ "status": "running", "progress": 0.55, "step": "Transcribing audio",
  "message": "...", "video_id": null, "error": null }
```
`status` is one of `queued | running | done | error`. The stream closes when
the job ends.

### Query
`POST /api/query` with `{ "text": "...", "top_k": 5 }`. The response shape
matches [utils/query.py](utils/query.py) — the frontend shows snippet,
timestamps, per-modality score breakdown, and surrounding context.

---

## Running locally

**Prereqs:** Python env with the main pipeline deps installed, plus the extra
server deps, plus a running Qdrant, plus `ffmpeg` on `PATH`, plus Node 18+ for
the frontend.

```bash
# backend deps (once)
pip install -r requirements.txt
pip install -r server/requirements.txt

# start Qdrant (example: docker)
docker run -p 6333:6333 qdrant/qdrant

# start the API
uvicorn server.api:app --reload --port 8000

# in another terminal: start the frontend
cd web
npm install
npm run dev
```

Open http://localhost:5173.

### Production build
```bash
cd web
npm run build    # emits web/dist/
```
Serve `web/dist` from any static host. Set a reverse proxy so `/api/*` reaches
the FastAPI process, or point the frontend at an absolute API base by setting
`BASE` in [web/src/api.js](web/src/api.js).

---

## File map

```
server/
├── api.py          FastAPI app: upload, jobs, videos, query
├── jobs.py         Job registry + pipeline runner with progress events
└── requirements.txt
web/
├── index.html
├── package.json
├── vite.config.js  dev proxy /api → :8000
└── src/
    ├── main.jsx        router setup
    ├── App.jsx         top navigation + layout
    ├── api.js          fetch / SSE / upload helpers
    ├── styles.css      full dark-mode stylesheet
    └── pages/
        ├── Search.jsx  user query UI, loading, result cards
        ├── Upload.jsx  upload form, upload %, pipeline %
        └── Admin.jsx   video list + active jobs, auto-refresh
```

---

## Design choices

**SSE over WebSockets.** One-way push from server → client is all we need for
progress. SSE works over plain HTTP, survives through the Vite proxy without
extra config, and reconnects automatically if you want it to.

**Background thread per job, not a task queue.** Keeps the deployment to a
single process. The ingestion pipeline is CPU-heavy (Whisper, CLIP), so one
concurrent job per host is the realistic limit anyway. For multi-host you'd
swap this for Celery/RQ — the `JobRegistry` interface is the seam.

**Qdrant is the video registry.** No separate DB. `/api/videos` aggregates
video metadata by scrolling payloads and grouping on `video_id`. Trade-off: a
scroll on every list call. Fine for dozens of videos; if this grows past a few
thousand, add a lightweight SQLite index or a dedicated Qdrant `videos`
collection.

**Upload size.** FastAPI+uvicorn stream the upload to disk 1 MB at a time, so
multi-GB files are fine as long as the disk has room. The browser shows true
byte-level upload progress via `XMLHttpRequest` (`fetch` doesn't expose
upload progress yet).

**Polling on Admin.** Auto-refresh every 3 s is simpler than SSE for a list
view and keeps the code small. If you have many users open, switch to SSE.

---

## Troubleshooting

- **CORS errors in the browser:** you're hitting the API on a different origin
  than the page served it. Use the Vite proxy (dev) or a reverse proxy (prod).
- **Upload starts at 100% instantly then hangs:** reverse proxy is buffering
  the upload. Disable proxy buffering for `/api/upload`.
- **Progress bar freezes at 55%:** Whisper is running. Transcription is the
  slowest step; there's no sub-progress inside it. On CPU, a 5-min video takes
  1–3 min at this step.
- **"Video already indexed":** same content hash exists in Qdrant. Tick
  **Re-index** on the upload form to overwrite.
- **Qdrant connection errors at startup:** the API won't fail to start, but
  `/api/videos` and `/api/query` will 500. Start Qdrant on
  `localhost:6333` (or change `QDRANT_HOST`/`QDRANT_PORT` in
  [config.py](config.py)).
