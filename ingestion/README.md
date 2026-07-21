# Ingestion

Pre-demo ingestion pipeline that extracts, chunks, embeds, and stores documents into pre-built vector indices. This module runs ahead of time, not during the live demo, and is designed to be presentable on-screen on its own. Each RAG mode (naive, docling-naive-chunk, docling-hybrid-chunk) produces its own vector index so the chat module can switch between them at runtime.

## Run

```shell
./mvnw -f ingestion quarkus:run
```
