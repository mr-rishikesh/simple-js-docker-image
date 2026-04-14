# Complete Setup & Usage Guide

Everything you need to install, run, and explore every feature of the
Video Retrieval system — from a fresh machine to your first successful search.

---

## Table of contents

1. [What this system does](#1-what-this-system-does)
2. [Prerequisites](#2-prerequisites)
3. [Installation](#3-installation)
4. [Starting the system](#4-starting-the-system)
5. [Feature walkthrough](#5-feature-walkthrough)
   - 5.1 [Upload a video](#51-upload-a-video)
   - 5.2 [Watch ingestion progress](#52-watch-ingestion-progress)
   - 5.3 [Search your videos](#53-search-your-videos)
   - 5.4 [Admin dashboard](#54-admin-dashboard)
6. [CLI usage (without the UI)](#6-cli-usage-without-the-ui)
7. [Config reference](#7-config-reference)
8. [Troubleshooting](#8-troubleshooting)
9. [Project file map](#9-project-file-map)

---

## 1. What this system does

You upload videos. The system:

- Extracts audio → **transcribes it** with Whisper (speech → text with timestamps)
- Extracts keyframes → runs **OCR** on them (on-screen text)
- Runs **CLIP** on those frames (visual embedding per time window)
- Breaks the video into **semantic chunks** (60–180 s each)
- Stores everything in **Qdrant** (vector DB) — two named vectors per chunk:
  `text` (384-dim) and `visual` (512-dim)

When you search, you type a plain English question. The system:

- Encodes your query into both spaces
- Does two searches in Qdrant (text + visual)
- Re-ranks by transcript relevance + visual match + topic density + recency +
  title overlap
- Returns the **best video(s)** with a timestamp, snippet, and score breakdown

---

## 2. Prerequisites

Install these before anything else.

### 2.1 Python 3.11 or 3.12

Download from https://www.python.org/downloads/ — tick **"Add to PATH"**
during install.

Verify:
```bash
python --version
# Python 3.11.x  or  3.12.x
```

### 2.2 Node.js 18 or later

Download from https://nodejs.org (LTS version). Required only for the web
frontend.

Verify:
```bash
node --version   # v18.x.x or higher
npm --version    # 9.x.x or higher
```

### 2.3 ffmpeg

**Windows (recommended — winget):**
```bash
winget install --id Gyan.FFmpeg -e
# restart your terminal after install so PATH updates
```

**Windows (manual):** download a build from https://www.gyan.dev/ffmpeg/builds/
— extract and add the `bin/` folder to your system PATH.

Verify:
```bash
ffmpeg -version
ffprobe -version
```
Both commands must respond — if either is missing the pipeline will fail at
the validation step.

### 2.4 Docker (for Qdrant)

Download from https://www.docker.com/products/docker-desktop

Verify:
```bash
docker --version
```

> **No Docker?** You can also run Qdrant as a Windows binary:
> download from https://github.com/qdrant/qdrant/releases, extract, run
> `qdrant.exe`. Default port is already 6333.

### 2.5 Git (optional, for cloning)

Only needed if you're cloning from a repo rather than working from a local
directory.

---

## 3. Installation

All commands below assume your terminal is open in the project root:
```
c:\ml projects\ml-based-video-recommendation-system\
```

### 3.1 Create and activate a Python virtual environment

```bash
python -m venv .venv
```

Activate it (you must do this every time you open a new terminal):
```bash
# Windows CMD
.venv\Scripts\activate.bat

# Windows PowerShell
.venv\Scripts\Activate.ps1

# Git Bash / MSYS2
source .venv/Scripts/activate
```

Your prompt changes to `(.venv) ...` when active.

### 3.2 Install Python dependencies

```bash
# Core pipeline
pip install -r requirements.txt

# API server (FastAPI, uvicorn)
pip install -r server/requirements.txt
```

This installs: PyTorch, Whisper, SentenceTransformers, open-clip-torch,
EasyOCR, Qdrant client, FastAPI, uvicorn, and a few utilities.

> **First run takes a while.** PyTorch alone is ~2 GB. On a slow connection,
> expect 5–20 minutes.

### 3.3 Install Node dependencies (frontend)

```bash
cd web
npm install
cd ..
```

This installs React, React Router, and Vite. It's fast — under a minute.

### 3.4 Verify all models will download automatically

The first time you run the pipeline, these models auto-download:
- **Whisper `base`** (~145 MB) — speech recognition
- **all-MiniLM-L6-v2** (~90 MB) — text embeddings
- **CLIP ViT-B/32** (~350 MB) — visual embeddings
- **EasyOCR English** (~50 MB) — OCR

They cache to your home directory (`~/.cache/`). Downloads only happen once.

---

## 4. Starting the system

You need **three terminals** running at the same time. Open all three in the
project root.

### Terminal 1 — Qdrant (vector database)

```bash
docker run -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

Wait for:
```
Qdrant HTTP listening on 0.0.0.0:6333
```

Leave this running. Qdrant stores all vector data. If you stop it, your
indexed videos are still there when you restart (data persists in Docker
volume by default).

> To persist data across Docker restarts, add a volume mount:
> ```bash
> docker run -p 6333:6333 -v qdrant_storage:/qdrant/storage qdrant/qdrant
> ```

### Terminal 2 — FastAPI backend

Make sure your virtual environment is activated first.

```bash
uvicorn server.api:app --reload --port 8000
```

Wait for:
```
INFO:     Uvicorn running on http://0.0.0.0:8000
INFO:     Application startup complete.
```

Leave this running. `--reload` means code changes auto-restart the server.
Remove `--reload` in production.

### Terminal 3 — React frontend

```bash
cd web
npm run dev
```

Wait for:
```
  VITE v5.x.x  ready in ...ms

  ➜  Local:   http://localhost:5173/
```

Leave this running.

### Open the app

Go to http://localhost:5173 in your browser.

You should see the app with three nav links: **Search**, **Upload**, **Admin**.

---

## 5. Feature walkthrough

### 5.1 Upload a video

1. Click **Upload** in the top navigation.

2. You see:
   - A dashed file drop area labeled "Choose a video file…"
   - A checkbox for "Re-index if this video is already in the database"
   - An **Upload & Index** button (disabled until a file is chosen)

3. Click the dashed area and select a video file from your computer.
   Supported formats: `.mp4` `.avi` `.mkv` `.mov` `.webm` `.flv` `.wmv`

4. The file name appears in the drop area. The button becomes active.

5. (Optional) Tick **Re-index** if you are uploading a video that you have
   already uploaded before and want to replace its index.

6. Click **Upload & Index**.

**What happens next is two phases:**

**Phase 1 — File upload** (first progress bar)
The file bytes travel from your browser to the server. A progress bar
shows the upload percentage (0 → 100%). For a 5-minute MP4 (~200 MB on
Wi-Fi this takes a few seconds; large files take longer).

**Phase 2 — Pipeline** (second progress bar, live-updating steps)
As soon as the upload finishes, the pipeline starts in the background.
The progress bar and step label update in real time via Server-Sent Events.
You'll see each stage announced:

| Progress | Step shown |
|---|---|
| 5% | Validating video |
| 15% | Extracting audio |
| 30% | Extracting keyframes |
| 55% | Transcribing audio ← slowest step on CPU |
| 60% | Creating semantic chunks |
| 75% | Running OCR on frames |
| 88% | Computing visual features |
| 93% | Generating text embeddings |
| 100% | Storing in vector database |

When done, the status changes to **✓ Complete** and a `video_id` is shown.

> **Transcription note:** on a CPU, Whisper takes roughly 20–60% of the
> video's duration. A 5-minute video ≈ 1–3 minutes at the "Transcribing"
> step. Progress will appear frozen here — it's working.

---

### 5.2 Watch ingestion progress

The progress page is live — you don't need to refresh. If you close the
Upload tab and come back:

- Go to **Admin** → the active-jobs panel at the top shows any running jobs
  with their current step and percentage.

- The Admin page auto-refreshes every 3 seconds.

Error states: if something goes wrong (bad video, Qdrant down, Whisper
crash) the status turns to **✗ Failed** with the error message. Fix the
underlying issue (e.g. restart Qdrant), re-upload with **Re-index** ticked.

---

### 5.3 Search your videos

1. Click **Search** in the top navigation (or go directly to
   http://localhost:5173/search).

2. You see a wide search input. Type any natural-language question or
   phrase — not just keywords:

   ```
   how does JWT authentication work
   what is the difference between SQL and NoSQL
   show me the configuration file
   explain gradient descent
   nginx server setup
   ```

3. Press **Enter** or click **Search**.

4. A spinner appears while the query runs (usually under 1 second once
   models are loaded).

5. Results appear — one card per video, sorted by relevance score.

**Reading a result card:**

```
#1                          jwt explained        score: 0.874
       jwt_explained.mp4

   1:02 – 3:00

   "so a JSON Web Token is basically three parts — a header, a payload,
   and a signature. The header specifies the algorithm..."

   on-screen text: header.payload.signature

   ▼ Score breakdown
   text: 0.812  visual: 0.641  density: 0.93  time×: 1.1  title×: 1.5
```

| Field | Meaning |
|---|---|
| **#1** | Rank — lower is better |
| **Score** | Final relevance score 0–1 (higher = more relevant) |
| **1:02 – 3:00** | The time window in the video that matched |
| **Snippet** | ~200 chars from the transcript at that moment |
| **on-screen text** | Text the OCR detected on screen during that window |
| **text score** | How well what was *said* matched your query |
| **visual score** | How well what was *shown* matched your query |
| **density** | Topic depth: 1.0 = sustained discussion, 0.0 = brief mention |
| **time×** | Recency multiplier (1.1 = < 6 months old, 0.8 = > 2 years) |
| **title×** | 1.5 if your query words appear in the video title |

**Tips for better search:**
- Full phrases work better than single words — the model understands intent.
- Ask questions the way you would ask a person.
- If you get no results, try broader terms (e.g. "authentication" instead of
  "JWT refresh token rotation").
- If results feel wrong, check the score breakdown — a very low text score
  with a high visual score means the video shows something related but
  doesn't say it.

---

### 5.4 Admin dashboard

Click **Admin** in the top navigation.

**Active jobs panel** (top)
Shows any ingestion jobs currently running or queued. Auto-updates every
3 s. When jobs finish they disappear from this panel but the video appears
in the list below.

**Indexed videos table**
One row per video with:

| Column | What it shows |
|---|---|
| **Title** | Cleaned version of the filename (underscores → spaces) |
| **Filename** | Original uploaded filename |
| **Duration** | Video length in `m:ss` |
| **Chunks** | Number of semantic chunks stored for this video |
| **Lang** | Language Whisper detected |
| **Added** | File modification date (when it was last written to disk) |
| **Delete** | Red button — removes the video and ALL its chunks from Qdrant |

**Deleting a video:**
Click **Delete** on any row → confirm the dialog → the row disappears on the
next 3-second refresh. This only removes the index in Qdrant. The uploaded
file in `data/uploads/` is not deleted (so you can re-index later).

---

## 6. CLI usage (without the UI)

You can also use the pipeline directly from the command line if you don't
want to use the web UI.

### Ingest a video

```bash
python main.py ingest path/to/video.mp4
```

Optional flags:
```bash
python main.py ingest video.mp4 --reindex       # force re-index
python main.py ingest video.mp4 --skip-ocr      # skip EasyOCR (faster)
python main.py ingest video.mp4 --skip-visual   # skip CLIP (faster)
python main.py ingest video.mp4 --keep-temp     # keep extracted frames/audio
```

### Query from the command line

```bash
python main.py query "how does authentication work"
python main.py query "nginx configuration" --top-k 3
python main.py query "what is gradient descent" --top-k 10
```

Output:
```
============================================================
  Result #1  |  Score: 0.874  |  [1:02 - 3:00]
  Video: jwt explained  (jwt_explained.mp4)
  Source: both
  Scores: text=0.812  visual=0.641  density=0.930  time=1.1  title_boost=1.5
  Text: so a JSON Web Token is basically three parts...
============================================================
```

### Ingest a batch of videos

```bash
# all MP4s in a directory
for f in videos/*.mp4; do python main.py ingest "$f"; done
```

---

## 7. Config reference

All tuneable settings live in [config.py](config.py). You don't need to edit
this for normal use, but it's useful to know what controls what.

### Chunking (most impactful for search quality)

```python
MIN_CHUNK_DURATION = 60       # minimum chunk length in seconds
MAX_CHUNK_DURATION = 180      # maximum chunk length in seconds
MIN_CHUNK_DURATION_HARD = 30  # any chunk shorter than this gets absorbed
SHORT_VIDEO_THRESHOLD = 420   # videos ≤7 min are indexed as ONE chunk
SIMILARITY_STD_FACTOR = 1.0   # higher = fewer splits (1.0 is conservative)
```

If you get wrong videos in results → try increasing `MIN_CHUNK_DURATION`
(more context per chunk = better embeddings).

### Qdrant connection

```python
QDRANT_HOST = "localhost"
QDRANT_PORT = 6333
COLLECTION_NAME = "video_chunks"
```

### Whisper model size

```python
WHISPER_MODEL = "base"   # base | small | medium | large
```

`base` is fastest. `medium` or `large` give significantly better
transcripts but take 3–5× longer and need more RAM.

### Search weights

```python
TEXT_WEIGHT   = 0.6   # weight for transcript/OCR match
VISUAL_WEIGHT = 0.3   # weight for visual/CLIP match
DENSITY_WEIGHT = 0.1  # weight for topic depth
TITLE_BOOST_FACTOR = 1.5  # multiplier when query matches video title
```

### Keyframe extraction

```python
SCENE_CHANGE_THRESHOLD = 0.3   # 0 = extract everything, 1 = almost nothing
MAX_KEYFRAMES_PER_MINUTE = 10  # cap for fast-cut videos
FALLBACK_FRAME_INTERVAL = 10   # seconds between frames if scene detection fails
```

---

## 8. Troubleshooting

### "ffmpeg not found"
ffmpeg is not on your PATH. See [section 2.3](#23-ffmpeg). After installing,
close and reopen your terminal.

### "Cannot reach Qdrant"
The Docker container is not running. Go to Terminal 1 and run the docker
command again. If you stopped the container with Ctrl+C, re-run:
```bash
docker run -p 6333:6333 qdrant/qdrant
```

### Upload succeeds but pipeline fails with "no video stream"
The file you uploaded may be audio-only (e.g. an MP3 renamed to .mp4) or
corrupted. Try a different file.

### Progress bar freezes at 55% (Transcribing)
This is normal. Whisper is running. On CPU with a 5-minute video, expect
1–3 minutes here. The bar will jump to 60% when it finishes. Do not reload.

### "Video already indexed" message
You uploaded the same video before (identified by content hash, not
filename). Tick **Re-index** on the upload form to overwrite it.

### Search returns no results
- Make sure at least one video has been indexed (check Admin).
- Try simpler search terms.
- Check that Qdrant is running (`docker ps` — look for the qdrant container).

### Search returns wrong videos
- The videos may have very short or unclear audio. Check the `detected_language`
  column in Admin — if it's wrong, transcription quality was poor.
- Low chunk count (1–2 chunks) on a long video suggests the chunker combined
  everything. Check the `Chunks` column in Admin.
- Try a query that more closely matches actual words spoken in the video.

### "ModuleNotFoundError" on startup
Your virtual environment is not activated. Run:
```bash
.venv\Scripts\activate   # Windows
```
Then retry the uvicorn or python command.

### EasyOCR error on first run
EasyOCR downloads its models on first use. If you're offline, this fails.
Run the pipeline once with internet access. After that it works offline.

### PowerShell says "cannot be loaded because running scripts is disabled"
Run this once in PowerShell as Administrator:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```
Then retry activating the venv.

### Frontend shows blank page / 404 on /api
Make sure the FastAPI server is running on port 8000 AND you opened the
Vite URL (port 5173), not the FastAPI URL (port 8000).

---

## 9. Project file map

```
ml-based-video-recommendation-system/
│
├── GUIDE.md               ← you are here
├── ARCHITECTURE.md        ← full data-flow and protocol documentation
├── FRONTEND.md            ← frontend-specific documentation
│
├── config.py              ← all tunable constants
├── main.py                ← CLI entrypoint (ingest / query)
├── requirements.txt       ← Python pipeline dependencies
│
├── server/
│   ├── api.py             ← FastAPI app (upload, SSE, videos, query)
│   ├── jobs.py            ← in-memory job registry + pipeline runner
│   └── requirements.txt  ← FastAPI/uvicorn deps
│
├── utils/
│   ├── validation.py      ← ffmpeg/Qdrant/file checks
│   ├── video.py           ← audio + keyframe extraction (ffmpeg)
│   ├── audio.py           ← Whisper transcription
│   ├── chunking.py        ← semantic chunking (sentence-transformers)
│   ├── ocr.py             ← EasyOCR on keyframes
│   ├── visual.py          ← CLIP visual embeddings
│   ├── embeddings.py      ← text embedding generation
│   ├── storage.py         ← Qdrant upsert / delete / list
│   ├── query.py           ← multi-signal search + re-ranking
│   └── cleanup.py         ← temp file removal
│
├── web/
│   ├── package.json
│   ├── vite.config.js     ← dev proxy /api → :8000
│   └── src/
│       ├── main.jsx       ← React router setup
│       ├── App.jsx        ← nav layout
│       ├── api.js         ← fetch / SSE / XHR helpers
│       ├── styles.css     ← dark-mode stylesheet
│       └── pages/
│           ├── Search.jsx ← user query UI
│           ├── Upload.jsx ← upload + progress UI
│           └── Admin.jsx  ← video list + active jobs
│
└── data/
    ├── uploads/           ← uploaded video files (permanent)
    ├── frames/            ← extracted keyframes (deleted after indexing)
    └── audio/             ← extracted WAV files (deleted after indexing)
```

---

## Quick-start checklist

Use this to verify everything before your first run:

- [ ] Python 3.11+ installed and on PATH
- [ ] Node.js 18+ installed
- [ ] ffmpeg installed and `ffmpeg -version` works in a new terminal
- [ ] Docker installed and running
- [ ] `python -m venv .venv` created and activated
- [ ] `pip install -r requirements.txt` completed without errors
- [ ] `pip install -r server/requirements.txt` completed without errors
- [ ] `cd web && npm install` completed without errors
- [ ] Terminal 1: `docker run -p 6333:6333 qdrant/qdrant` running
- [ ] Terminal 2: `uvicorn server.api:app --reload --port 8000` running
- [ ] Terminal 3: `cd web && npm run dev` running
- [ ] http://localhost:5173 opens without errors
- [ ] Uploaded one test video and saw "✓ Complete"
- [ ] Ran a search query and got at least one result
