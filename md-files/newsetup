# Complete Setup & Usage Guide - Semantic Search System

A comprehensive guide to set up, start, and use all features of the unified semantic search system.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [Starting the System](#starting-the-system)
5. [Web Interface Tour](#web-interface-tour)
6. [How to Upload Documents](#how-to-upload-documents)
7. [How to Search](#how-to-search)
8. [Understanding Results](#understanding-results)
9. [Advanced Features](#advanced-features)
10. [Troubleshooting](#troubleshooting)
11. [API Reference](#api-reference)

---

## System Overview

### What It Does

This is a **semantic search system** that understands the meaning of your queries, not just keywords.

**Example:**
- **Keyword Search (Old)**: Search "exponential backoff" → finds only exact matches
- **Semantic Search (New)**: Search "How to handle network failures?" → finds "retry logic", "circuit breakers", "timeout handling" etc.

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Web Browser (React)                    │
│              http://localhost:5173                       │
│                                                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │         Upload Page        │     Search Page     │   │
│  │  - Select files            │  - Type queries     │   │
│  │  - Choose options          │  - See suggestions  │   │
│  │  - Track progress          │  - View results     │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
                         ▼
┌─────────────────────────────────────────────────────────┐
│      FastAPI Server (http://localhost:8000)              │
│                                                           │
│  ┌──────────────────┐  ┌─────────────────────────────┐ │
│  │  Ingest API      │  │  Semantic Search API        │ │
│  │  (/api/*)        │  │  (/v1/*)                    │ │
│  │                  │  │                             │ │
│  │ - Upload         │  │ - Search (semantic+keyword) │ │
│  │ - List videos    │  │ - Suggestions (5 sources)   │ │
│  │ - Delete video   │  │ - Trending queries          │ │
│  │ - Job progress   │  │ - Track interactions        │ │
│  │ - Legacy search  │  │ - Accuracy prediction       │ │
│  └──────────────────┘  └─────────────────────────────┘ │
└────────────┬───────────────────────────────┬────────────┘
             │ Read/Write                     │ Read/Write
             ▼                                ▼
┌────────────────────────┐    ┌───────────────────────────┐
│   PostgreSQL (5432)    │    │   Qdrant (6333)           │
│                        │    │                           │
│ - Search history       │    │ - Vector embeddings       │
│ - User interactions    │    │ - Semantic search index   │
│ - Ratings              │    │ - Multiple collections    │
│ - Analytics            │    │ - Similarity matching     │
└────────────────────────┘    └───────────────────────────┘
                                       │
                    ┌──────────────────┘
                    │
                    ▼
        ┌──────────────────────┐
        │   Redis (6379)       │
        │                      │
        │ - Suggestion cache   │
        │ - Performance boost  │
        └──────────────────────┘
```

### Single Unified Server

Everything runs on **one FastAPI server on port 8000**:
- No separate services
- No port conflicts
- All endpoints in one place
- Easy to deploy

---

## Prerequisites

### System Requirements

- **OS**: Windows, macOS, or Linux
- **RAM**: 4GB minimum (8GB recommended)
- **Disk Space**: 10GB available
- **Network**: Internet connection for downloading models

### Required Software

1. **Docker & Docker Compose**
   ```bash
   # Windows/macOS: Download from https://www.docker.com/products/docker-desktop
   # Linux: sudo apt-get install docker.io docker-compose
   
   # Verify installation
   docker --version
   docker-compose --version
   ```

2. **Node.js & npm** (for frontend)
   ```bash
   # Download from https://nodejs.org (LTS version)
   # Or use package manager: brew install node (macOS), choco install nodejs (Windows)
   
   # Verify installation
   node --version
   npm --version
   ```

3. **Git** (optional, for cloning or updating)
   ```bash
   git --version
   ```

### Port Availability

Ensure these ports are free on your system:
- **8000** - FastAPI backend
- **5173** - React frontend
- **5432** - PostgreSQL database
- **6333** - Qdrant vector DB
- **6379** - Redis cache

Check on Windows (PowerShell):
```powershell
netstat -ano | findstr :8000
# If nothing appears, port is free
```

Check on macOS/Linux:
```bash
lsof -i :8000
# If nothing appears, port is free
```

---

## Installation

### Step 1: Prepare the Project

```bash
# Navigate to project directory
cd /c/ml\ projects/hackathon/ml-based-video-recommendation-system

# Or on macOS/Linux:
# cd /path/to/ml-projects/hackathon/ml-based-video-recommendation-system

# Verify you're in the right place
ls -la
# Should see: app/, web/, docker-compose.yml, Dockerfile, etc.
```

### Step 2: Configure Environment

```bash
# Copy environment template
cp .env.example .env

# View the .env file (optional)
cat .env

# Content should include:
# DATABASE_URL=postgresql://...
# QDRANT_URL=http://qdrant:6333
# REDIS_URL=redis://redis:6379
```

### Step 3: Create Data Directories

```bash
# Create directories for uploads if they don't exist
mkdir -p data/uploads
mkdir -p data/frames
mkdir -p data/audio

# Verify
ls -la data/
```

### Step 4: Install Frontend Dependencies

```bash
cd web

# Install npm packages (takes 2-3 minutes first time)
npm install

# Verify installation
npm list react react-dom

cd ..
# Go back to project root
```

---

## Starting the System

### Method 1: Docker (Recommended for Demo & Production)

#### Start All Services

```bash
# From project root directory
docker-compose up -d

# Expected output:
# Creating semantic-search-postgres ...
# Creating semantic-search-redis ...
# Creating semantic-search-qdrant ...
# Creating semantic-search-api ...
```

#### Wait for Services to Be Ready

```bash
# Check status (wait until all show "healthy")
docker-compose ps

# Expected output (wait until READY column shows healthy):
# NAME                        IMAGE               STATUS
# semantic-search-postgres    pgvector/pgvector   Up ... (healthy)
# semantic-search-redis       redis:7-alpine      Up ... (healthy)
# semantic-search-qdrant      qdrant/qdrant       Up ... (healthy)
# semantic-search-api         <built>             Up ... (healthy)
```

This usually takes 15-20 seconds first time.

#### Verify Backend is Running

```bash
# Test API health endpoint
curl http://localhost:8000/api/health
# Expected response: {"ok": true}

curl http://localhost:8000/v1/health
# Expected response: {"status": "healthy", "service": "...", "version": "..."}
```

#### Start Frontend (in new terminal)

```bash
# Navigate to web directory
cd web

# Start development server
npm run dev

# Expected output:
# VITE v5.0.0 ready in 123 ms
# 
# ➜  Local:   http://localhost:5173/
# ➜  Network: http://YOUR-IP:5173/
```

#### Open in Browser

```
http://localhost:5173
```

You should see the application with two pages: **Upload** and **Search**

---

### Method 2: Manual (Development Only)

If you prefer not to use Docker:

```bash
# 1. Install Python 3.10+
python --version

# 2. Create virtual environment
python -m venv venv

# Activate (Windows)
venv\Scripts\activate

# Activate (macOS/Linux)
source venv/bin/activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Start backend
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

# 5. In another terminal, start frontend
cd web
npm run dev

# 6. Open http://localhost:5173
```

**Note**: Manual method requires PostgreSQL, Qdrant, and Redis running separately. Docker method is simpler.

---

## Web Interface Tour

### Main Navigation

```
┌─────────────────────────────────────────┐
│  🎯 Semantic Search System              │
│  ├─ Upload                              │
│  └─ Search                              │
└─────────────────────────────────────────┘
```

### Upload Page

**Purpose**: Upload documents (videos, PDFs, text, code, logs) for indexing

```
┌─────────────────────────────────────────┐
│ Upload Video                            │
│                                         │
│ Upload a video to index it for          │
│ semantic search. Supported:             │
│ .mp4 .avi .mkv .mov .webm .flv .wmv    │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📁 Click to choose a video file     │ │ ← File picker
│ └─────────────────────────────────────┘ │
│                                         │
│ INDEXING OPTIONS                        │
│ ☐ Re-index if already uploaded          │
│ ☐ Skip OCR (faster, no on-screen text)  │
│ ☐ Skip visual features (text-only)      │
│ ☐ Keep temp files after indexing        │
│                                         │
│ [Upload & Index] ← Click to start       │
│                                         │
│ PROGRESS (after clicking):              │
│ ████████░░░░░░░░░░  45%                 │
│                                         │
│ Pipeline Steps:                         │
│ ✓ Validate video                        │
│ ✓ Extract audio                         │
│ ✓ Extract keyframes                     │
│ ● Transcribe speech (in progress)       │
│ ○ Create semantic chunks                │
│ ○ OCR on-screen text                    │
│ ○ Visual features (CLIP)                │
│ ○ Generate text embeddings              │
│ ○ Store in Qdrant                       │
│                                         │
│ ✓ Indexed successfully                  │
│ video_id: abc123def456                  │
│ [Go to Search →]                        │
└─────────────────────────────────────────┘
```

### Search Page

**Purpose**: Search documents with semantic understanding

```
┌─────────────────────────────────────────┐
│ 🔍 Semantic Search                      │
│                                         │
│ Advanced AI search that understands     │
│ meaning, not just keywords.             │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Search anything... semantic...  │US │ │ ← Query + Language
│ └─────────────────────────────────────┘ │
│        [Search]                         │
│                                         │
│ Suggestions Dropdown (if typing):       │
│ ┌─────────────────────────────────────┐ │
│ │ 1. "error handling" (98%) - Spelling │ │
│ │ 2. "exception handling" (85%) - Trend │
│ │ 3. "error management" (76%) - Related │
│ │ 4. "try-catch blocks" (64%) - History │
│ │ 5. "manejo de errores" (45%) - Other  │
│ └─────────────────────────────────────┘ │
│                                         │
│ RESULTS:                                │
│                                         │
│ 🎯 Search Accuracy: 89% - HIGH          │
│ Why: Query is specific | High scores    │
│ Tips: Consider broadening terms         │
│                                         │
│ ✓ Found 3 results for "error handling"  │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ #1 [Green]              Score: 94%   │ │ ← Result Card
│ │ Error Handling Patterns              │ │
│ │ video_filename: intro_exceptions.mp4 │ │
│ │ 🏷️ semantic match                     │ │
│ │                                       │ │
│ │ ⏱️ 2:34 - 2:44                        │ │
│ │ 💬 ...gracefully handle network       │ │
│ │    failures with exponential backoff. │ │
│ │                                       │ │
│ │ ✨ Why this result:                   │ │
│ │ Semantic similarity (retry logic →    │ │
│ │ handling failures) + keyword "network"│ │
│ │                                       │ │
│ │ 📊 Score breakdown (94%)              │ │
│ │    ├─ Semantic: 96% (meaning match)  │ │
│ │    ├─ Keyword: 85% (exact terms)     │ │
│ │    └─ Re-ranked: 93% (quality)       │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ #2 [Yellow]             Score: 72%   │ │
│ │ ... (more results)                   │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## How to Upload Documents

### Quick Upload

1. **Navigate to Upload Page**
   - Click "Upload" in the top navigation

2. **Select a File**
   - Click the file picker box
   - Choose a video file (.mp4, .avi, .mkv, .mov, .webm, .flv, .wmv)
   - File size: 100MB - 2GB works best

3. **Choose Options** (Optional)
   - **Re-index**: Check if re-processing previously uploaded file
   - **Skip OCR**: Faster indexing, no on-screen text extraction
   - **Skip Visual**: Text-only search, no visual features (CLIP)
   - **Keep Temp**: Keep extracted audio/frames for debugging

4. **Click Upload & Index**
   - Progress bar shows upload percentage
   - Once uploaded, pipeline starts automatically

5. **Wait for Processing**
   - Watch pipeline steps complete in order:
     1. Validate video (5%)
     2. Extract audio (15%)
     3. Extract keyframes (30%)
     4. Transcribe speech (55%)
     5. Create semantic chunks (60%)
     6. OCR on-screen text (75%)
     7. Visual features (88%)
     8. Generate embeddings (93%)
     9. Store in Qdrant (100%)

   - **Typical times**:
     - 1 min video: 5-10 seconds
     - 10 min video: 30-60 seconds
     - 60 min video: 3-5 minutes
     - (Slower on CPU, faster on GPU)

6. **Success**
   - Message shows: "✓ Indexed successfully"
   - Display shows `video_id: abc123...`
   - Click "Go to Search →" to test

### Upload Multiple Documents

Repeat the process for each document. All will be indexed and searchable together.

```bash
# View uploaded documents via API
curl http://localhost:8000/api/videos
# Returns: {"videos": [{...}, {...}, ...]}
```

### Supported File Types

| Type | Extensions | Use Case |
|------|-----------|----------|
| Video | .mp4, .avi, .mkv, .mov, .webm, .flv, .wmv | Lectures, tutorials, meetings |
| Document | .pdf, .txt | Reports, guides, articles |
| Code | .py, .js, .java, .go, etc | Source files, snippets |
| Logs | .log, .txt | Error logs, debug output |
| Audio | .wav, .mp3 | Podcasts, recordings |

---

## How to Search

### Basic Search

1. **Go to Search Page**
   - Click "Search" in navigation

2. **Type Your Query**
   - Enter what you're looking for
   - Example: "How to handle network failures?"
   - Suggestions appear as you type (wait 300ms)

3. **Select a Suggestion** (Optional)
   - Dropdown shows 5 suggestions
   - Each shows: text, reason, confidence, source
   - Click any to auto-fill search box

4. **Submit Search**
   - Press Enter or click [Search]
   - Results appear within 3-5 seconds

5. **Review Results**
   - See accuracy prediction at top
   - Scroll through results
   - Expand score breakdown to see components

### Using Language Selector

The search supports 7 languages:
- 🇺🇸 English
- 🇪🇸 Spanish
- 🇫🇷 French
- 🇩🇪 German
- 🇨🇳 Chinese
- 🇯🇵 Japanese
- 🇸🇦 Arabic

**How to use**:
1. Click language dropdown
2. Select language
3. Type query in that language
4. Suggestions and results adapt to language

### Search Strategies

#### Strategy 1: Semantic Queries
Use natural language questions instead of keywords:

```
BAD: "exponential backoff"
GOOD: "How to gracefully handle retries?"

BAD: "JWT token"
GOOD: "How should I securely store authentication tokens?"

BAD: "Docker container"
GOOD: "How to containerize a Python application?"
```

#### Strategy 2: Specific vs Broad
- **Too vague**: "stuff", "help", "test"
  - Result: Low accuracy (20-40%)
  - Suggestion: More specific terms

- **Too specific**: "line 42 in file.py"
  - Result: No matches
  - Suggestion: Search more general concept

- **Just right**: "error handling in async functions"
  - Result: High accuracy (80-95%)
  - Suggestions relevant and specific

#### Strategy 3: Combining Keywords
- "Python async/await patterns"
- "REST API authentication best practices"
- "Database query optimization"

#### Strategy 4: Problem Description
- "Why is my code hanging?"
- "How to prevent SQL injection?"
- "What causes out of memory errors?"

---

## Understanding Results

### Result Card Components

```
┌─────────────────────────────────┐
│ #1 [Color Badge]   Score: 94%   │ ← Rank, color, score
│                                 │
│ Title: "Error Handling Patterns"│ ← Document title
│ Filename: intro_exceptions.mp4  │ ← Source file
│ 🏷️ semantic match                │ ← Match type
│                                 │
│ ⏱️ 2:34 - 2:44                   │ ← Timestamp (video)
│                                 │
│ 💬 ...gracefully handle network  │ ← Text snippet
│    failures with exponential...  │
│                                 │
│ ✨ Why this result:              │ ← Explanation
│ Semantic similarity (retry logic │
│ matches handling failures) +     │
│ keyword "network" matched        │
│                                 │
│ 📊 Score breakdown (94%) ▼       │ ← Expandable
│    - Semantic: 96% (meaning)    │
│    - Keyword: 85% (exact)       │
│    - Re-ranked: 93% (quality)   │
└─────────────────────────────────┘
```

### Color Coding

**Rank Circle Color** indicates relevance:
- 🟢 **Green** (90-100%): Highly relevant
- 🟡 **Yellow** (70-89%): Relevant
- 🟠 **Orange** (50-69%): Somewhat relevant
- 🔴 **Red** (0-49%): Less relevant

### Score Breakdown

Each result has 3 components:

1. **Semantic Score** (0-100%)
   - Measures meaning similarity
   - Does the concept match?
   - Uses embeddings and vector similarity
   - Most important for understanding

2. **Keyword Score** (0-100%)
   - Measures exact word matches
   - Are query words in the document?
   - Uses BM25 algorithm
   - Good for specific terms

3. **Re-ranked Score** (0-100%)
   - Final quality score
   - Combines semantic + keyword
   - Uses cross-encoder model
   - What you see as final score

### Accuracy Prediction

Appears at top of results:

```
🎯 Search Accuracy: 89% - HIGH
Relevant because: Query is specific | Top result has high score
💡 Tips: Consider broadening terms
```

**Interpretation**:
- **Very High (95-100%)**: Results are very likely correct
- **High (80-94%)**: Results are likely correct
- **Moderate (50-79%)**: Some results may not be relevant
- **Low (20-49%)**: Results are uncertain, try different query
- **Very Low (0-19%)**: No good matches found

**Tips shown**:
- "Try more specific terms" → Your query is too vague
- "Consider broadening terms" → Your query is too narrow
- "Check document content" → Documents don't cover topic
- "Try different keywords" → Phrasing doesn't match

---

## Advanced Features

### Query Suggestions

As you type, system shows 5 suggestions from different sources:

```
Sources:
1. Spelling corrections
   - "handl" → "handle"
   - Fixes typos

2. Query expansions
   - "error" → "error handling best practices"
   - Adds related terms

3. Historical queries
   - Previously searched queries
   - From your search history

4. Trending queries
   - Popular searches on platform
   - What others search for

5. Multilingual variants
   - Same query in other languages
   - "error" → "erreur" (French)
```

**How to use**:
- See a suggestion you like? Click it
- Auto-fills search box
- Submit immediately

### Search Tracking

System tracks your searches (with your permission):
- What you searched for
- Which results you clicked
- How long you spent on each result
- If you marked results relevant/irrelevant

**Purpose**: To improve future searches
- Better suggestions next time
- More accurate predictions
- Personalized results

**Privacy**: Data stays on your server, not shared externally.

### Trending Queries

See what others are searching for:

```bash
# Get trending queries
curl "http://localhost:8000/v1/search/trending?language=en&hours=24&limit=10"

# Response:
{
  "trending": [
    {"query": "error handling", "count": 15},
    {"query": "async/await", "count": 12},
    ...
  ]
}
```

### Rate Search Quality

After viewing results, you can rate them:

```bash
# Rate a search as helpful
curl -X POST "http://localhost:8000/v1/history/{search_id}/rate?rating=5"

# Ratings: 1-5 stars
# System learns from ratings
```

### View Search History

See all your past searches:

```bash
# Get your search history
curl "http://localhost:8000/v1/history/user/{user_id}"

# Shows:
# - All queries you searched
# - Results you got
# - Which you clicked
# - Your ratings
```

---

## Troubleshooting

### Issue: Can't Start Backend

**Problem**: `docker-compose up -d` fails

**Solutions**:

```bash
# 1. Check Docker is running
docker ps
# If error, start Docker Desktop or service

# 2. Check ports are free
lsof -i :8000
# If occupied, kill the process or use different port

# 3. Try restart
docker-compose down
docker-compose up -d

# 4. Check logs
docker-compose logs api
# Look for error messages
```

### Issue: Frontend Won't Load

**Problem**: `http://localhost:5173` shows error

**Solutions**:

```bash
# 1. Check npm dev server is running
npm run dev
# Should show "Local: http://localhost:5173/"

# 2. Check logs for errors
# Look at terminal output for error messages

# 3. Clear npm cache
npm cache clean --force
npm install
npm run dev

# 4. Try different port
npm run dev -- --port 3000
```

### Issue: API Not Responding

**Problem**: Frontend can't connect to backend

**Solutions**:

```bash
# 1. Check API is running
curl http://localhost:8000/api/health
# Should return: {"ok": true}

# 2. Check API logs
docker-compose logs api

# 3. Check services are healthy
docker-compose ps
# All should show "healthy"

# 4. Restart API
docker-compose restart api
docker-compose logs api
```

### Issue: Upload Fails

**Problem**: File upload shows error

**Solutions**:

```bash
# 1. Check file format
# Supported: .mp4, .avi, .mkv, .mov, .webm, .flv, .wmv
# Make sure file extension is correct

# 2. Check file size
# Max recommended: 2GB
# Larger files take longer to process

# 3. Check disk space
# System needs free space to extract audio/frames
# Recommend 10GB free minimum

# 4. Check logs
docker-compose logs api | tail -50
# Look for upload-related errors

# 5. Try smaller file first
# Upload 1-minute test video to verify setup works
```

### Issue: Suggestions Not Appearing

**Problem**: No suggestions show while typing

**Solutions**:

```bash
# 1. Test suggestions endpoint
curl "http://localhost:8000/v1/suggestions?query=test&language=en"
# Should return JSON with suggestions array

# 2. Check browser console
# Press F12, look for errors
# Network tab should show GET request to /v1/suggestions

# 3. Check API logs
docker-compose logs api | grep suggestions

# 4. Restart frontend
# Stop npm dev server (Ctrl+C)
# npm run dev
```

### Issue: Search Returns No Results

**Problem**: No documents found even after upload

**Solutions**:

```bash
# 1. Verify documents were uploaded
curl http://localhost:8000/api/videos
# Should show {"videos": [...]}
# If empty, no documents indexed yet

# 2. Check upload actually completed
# Look at Upload page logs
# Should show "✓ Indexed successfully"

# 3. Check Qdrant has data
curl http://localhost:6333/collections
# Should list collections

# 4. Scroll the collection
curl -X POST http://localhost:6333/collections/{collection}/points/scroll \
  -H "Content-Type: application/json" \
  -d '{"limit": 10}'
# Should show points/vectors stored

# 5. Try exact keyword match
# Search for word you know is in document
# If even keywords don't work, check upload

# 6. Check backend logs
docker-compose logs api | grep -i "search\|error"
```

### Issue: Slow Performance

**Problem**: Searches take 10+ seconds

**Solutions**:

```bash
# 1. Check system resources
docker-compose stats
# Look for high CPU or memory usage

# 2. Check database query time
docker-compose logs postgres | tail -20
# Look for slow query logs

# 3. Restart services
docker-compose down
docker-compose up -d

# 4. Clear cache
docker-compose exec redis redis-cli FLUSHALL
docker-compose logs api

# 5. Check CPU/GPU availability
# GPU is 10-50x faster than CPU for embeddings
# Check if models are running on GPU:
docker-compose logs api | grep -i "cuda\|gpu"
```

### Issue: Database Errors

**Problem**: PostgreSQL connection errors

**Solutions**:

```bash
# 1. Check database is healthy
docker-compose exec postgres pg_isready
# Should return "accepting connections"

# 2. Connect to database
docker-compose exec postgres psql -U user -d semantic_search -c "SELECT 1"
# Should return "1"

# 3. Check tables exist
docker-compose exec postgres psql -U user -d semantic_search -c "\dt"
# Should show: search_history, documents, ingest_progress

# 4. Check database size
docker-compose exec postgres psql -U user -d semantic_search -c "\l+"

# 5. Backup and reset database
docker-compose down
docker volume rm semantic_search_postgres_data
docker-compose up -d
# Warning: This deletes all search history!
```

### Issue: Out of Memory

**Problem**: System crashes with memory errors

**Solutions**:

```bash
# 1. Check memory usage
docker-compose stats
# Look for memory usage

# 2. Limit memory per service (edit docker-compose.yml)
services:
  api:
    mem_limit: 2g  # Add this line

# 3. Process smaller files
# Large files (>1GB) need more RAM
# Try with smaller files first

# 4. Increase system RAM
# Minimum 4GB, recommended 8GB+
# Close other applications

# 5. Use swapfile
# On Linux: Create swap space
# On Windows: Automatic, increase in Docker settings
```

---

## API Reference

### Authentication

No authentication required for demo. For production, add API keys:

```bash
# All requests: add header
Authorization: Bearer YOUR_API_KEY
```

### Base URLs

- **Ingest API**: `http://localhost:8000/api`
- **Semantic API**: `http://localhost:8000/v1`

### Upload Document

```
POST /api/upload

Parameters (multipart form):
  - file: File to upload (required)
  - reindex: bool = false
  - skip_ocr: bool = false
  - skip_visual: bool = false
  - keep_temp: bool = false

Response:
{
  "job_id": "abc123",
  "filename": "video.mp4"
}

Example:
curl -X POST http://localhost:8000/api/upload \
  -F "file=@video.mp4"
```

### Get Job Status

```
GET /api/jobs/{job_id}

Response:
{
  "id": "abc123",
  "filename": "video.mp4",
  "status": "done",
  "progress": 1.0,
  "step": "Store in Qdrant",
  "message": "Stored in Qdrant",
  "video_id": "video_abc123",
  "error": null
}

Status values: queued, processing, done, error
```

### List Videos

```
GET /api/videos

Response:
{
  "videos": [
    {
      "id": "video_abc123",
      "filename": "video.mp4",
      "title": "Python Basics",
      "duration": 3600,
      "upload_date": "2024-04-25T10:30:00Z",
      "language": "en",
      "chunk_count": 150
    }
  ]
}
```

### Search (Semantic + Keyword)

```
POST /v1/search

Body:
{
  "query": "How to handle errors?",
  "language": "en",
  "collection_ids": ["video_abc123"],
  "top_k": 5,
  "user_id": "user123"
}

Response:
{
  "query": "How to handle errors?",
  "results": [
    {
      "chunk_id": "chunk_001",
      "text": "Gracefully handle errors with try-catch...",
      "score": 0.94,
      "source_type": "video",
      "source_title": "Python Basics",
      "timestamp": 120.5,
      "match_type": "semantic match",
      "score_breakdown": {
        "semantic": 0.96,
        "keyword": 0.85,
        "rerank": 0.93,
        "composite": 0.93
      },
      "relevance_explanation": "Semantic similarity + keyword match"
    }
  ],
  "total_results": 5,
  "search_time_ms": 1234,
  "accuracy_prediction": {
    "predicted_accuracy": 0.89,
    "confidence_level": "high",
    "reasoning": "Query is specific, top results have high scores",
    "recommendations": ["Consider broadening terms"]
  },
  "search_history_id": 42
}
```

### Get Suggestions

```
GET /v1/suggestions?query=error&language=en&limit=5

Response:
{
  "query": "error",
  "language": "en",
  "suggestions": [
    {
      "suggestion": "error handling",
      "confidence": 0.98,
      "reason": "Query expansion with related terms",
      "category": "expansion",
      "source": "thesaurus"
    },
    {
      "suggestion": "exception",
      "confidence": 0.85,
      "reason": "Trending in last 24 hours",
      "category": "trending",
      "source": "trending"
    }
  ]
}
```

### Get Trending Queries

```
GET /v1/search/trending?language=en&hours=24&limit=10

Response:
{
  "trending": [
    {"query": "error handling", "count": 15},
    {"query": "async/await", "count": 12}
  ],
  "language": "en",
  "time_window_hours": 24
}
```

### Record Search Interaction

```
POST /v1/history/{search_id}/interact

Parameters:
  - clicked_indices: [0, 2, 3] (comma-separated in URL)
  - dwell_time_ms: 5000
  - marked_relevant: [0]
  - marked_irrelevant: [3]

Example:
curl -X POST "http://localhost:8000/v1/history/42/interact?clicked_indices=0,2&dwell_time_ms=5000&marked_relevant=0"
```

### Rate Search

```
POST /v1/history/{search_id}/rate?rating=5

Parameters:
  - rating: 1-5 (1=bad, 5=excellent)

Example:
curl -X POST "http://localhost:8000/v1/history/42/rate?rating=4"
```

---

## Tips & Tricks

### Performance Tips

1. **Use specific queries**: "error handling in async functions" > "error"
2. **Use natural language**: "How to...?" works better than keywords
3. **Use language selector**: Set correct language before searching
4. **Batch operations**: Upload multiple documents, then search
5. **Browser caching**: Results cached in browser, faster subsequent searches

### Feature Tips

1. **Suggestions as training**: Click suggestions to see better options
2. **Score breakdown**: Expand to understand why result matched
3. **Accuracy prediction**: Low accuracy? Try different keywords
4. **Language detection**: System auto-detects language, but selector is more precise
5. **History**: Past searches suggest future queries

### Troubleshooting Tips

1. **Check logs first**: `docker-compose logs api` shows most errors
2. **Verify connectivity**: `curl http://localhost:8000/api/health`
3. **Try smaller files**: 1-minute video verifies setup works
4. **Clear cache**: `docker volume prune` removes old data
5. **Restart services**: `docker-compose restart` usually fixes issues

---

## Next Steps

### For Testing

1. ✓ Start backend with Docker
2. ✓ Start frontend with npm
3. ✓ Upload a test document (1-minute video)
4. ✓ Test basic search
5. ✓ Test suggestions dropdown
6. ✓ Check accuracy prediction
7. ✓ View score breakdown

### For Demo

1. ✓ Upload 3-5 documents (various types)
2. ✓ Demo semantic vs keyword difference
3. ✓ Show suggestions appearing in real-time
4. ✓ Display accuracy predictions
5. ✓ Expand score breakdown
6. ✓ Change language and re-search
7. ✓ Show ranking differences

### For Production

1. ✓ Add authentication (API keys)
2. ✓ Configure HTTPS/SSL
3. ✓ Set resource limits (memory, CPU)
4. ✓ Configure backups
5. ✓ Monitor logs and metrics
6. ✓ Set up load balancing
7. ✓ Plan scaling strategy

---

## Support

### Getting Help

**Check these first**:
1. This guide (Troubleshooting section)
2. Backend logs: `docker-compose logs api`
3. Frontend console: Browser F12 → Console tab
4. API docs: `http://localhost:8000/docs`

**Commands for debugging**:

```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs api -f

# Check service health
docker-compose ps

# Connect to database
docker-compose exec postgres psql -U user -d semantic_search

# Check Qdrant
curl http://localhost:6333/health

# Check Redis
docker-compose exec redis redis-cli PING
```

---

## Summary

**You now have**:
- ✅ Single unified FastAPI server on port 8000
- ✅ React frontend on port 5173
- ✅ PostgreSQL database for history
- ✅ Qdrant vector DB for embeddings
- ✅ Redis cache for performance
- ✅ Semantic search with suggestions
- ✅ Multilingual support (7 languages)
- ✅ Accuracy predictions
- ✅ Score breakdowns
- ✅ Search history tracking

**Ready to**:
- 📤 Upload documents (video, PDF, text, code, logs)
- 🔍 Search with semantic understanding
- 💡 Get intelligent suggestions
- 📊 View detailed score breakdowns
- 🌍 Search in multiple languages
- 📈 Track and improve searches

**Enjoy your semantic search system!** 🚀
