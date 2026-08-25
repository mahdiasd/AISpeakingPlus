# codebase-memory-mcp — Quick Command Reference

A practical cheat sheet for the `codebase-memory-mcp` CLI. All commands run from your terminal. Replace `<PROJECT>` with the project name returned by `list_projects` (usually derived from the repo path with slashes replaced by dashes, e.g. `Users-mahdi-StudioProjects-MyApp`).

---

## 0. First-time setup (already done on this machine)

```bash
# One-line install (skip auto-config to avoid touching other agents)
curl -fsSL https://raw.githubusercontent.com/DeusData/codebase-memory-mcp/main/install.sh | bash -s -- --skip-config --dir ~/.local/bin

# Confirm
codebase-memory-mcp --version
```

Hermes MCP server entry (in `~/.hermes/config.yaml`):
```yaml
mcp_servers:
  codebase_memory:
    command: /Users/mahdi/.local/bin/codebase-memory-mcp
```

---

## 1. Project discovery & indexing

### List all indexed projects
```bash
codebase-memory-mcp cli list_projects
```

### Index the current repo
```bash
# Full mode — all files + similarity/semantic edges (can be slow, may crash on some files)
codebase-memory-mcp cli index_repository --repo-path . --mode full

# Fast mode — filtered files, no similarity/semantic edges (recommended first pass)
codebase-memory-mcp cli index_repository --repo-path . --mode fast

# Moderate — filtered files + similarity/semantic
codebase-memory-mcp cli index_repository --repo-path . --mode moderate
```

### Check index status
```bash
codebase-memory-mcp cli index_status --project <PROJECT>
```

### Delete a project from the index
```bash
codebase-memory-mcp cli delete_project --project <PROJECT>
```

### Re-index after changes
```bash
# Auto-sync via background watcher is enabled by default. To manually re-index:
codebase-memory-mcp cli index_repository --repo-path . --mode fast
```

### Enable auto-indexing on session start
```bash
codebase-memory-mcp config set auto_index true
codebase-memory-mcp config set auto_watch true   # background watcher for git-based change detection
```

### Persist a team-shared graph artifact (`.codebase-memory/graph.db.zst`)
```bash
codebase-memory-mcp cli index_repository --repo-path . --mode fast --persistence true
```
Teammates cloning the repo skip the full reindex — the artifact is imported first, then incremental indexing fills in their local diff.

---

## 2. Architecture overview

### Full overview (languages, packages, entry points, routes, hotspots, boundaries, layers, clusters)
```bash
codebase-memory-mcp cli get_architecture --project <PROJECT>
```

### Specific aspects only
```bash
codebase-memory-mcp cli get_architecture --args-file <(echo '{
  "project": "<PROJECT>"
}' | jq '.aspects = ["overview"]')

# Available aspects: overview, structure, dependencies, routes, languages,
# packages, entry_points, hotspots, boundaries, layers, file_tree, clusters, all
```

### File tree
```bash
codebase-memory-mcp cli get_architecture --args-file <(echo '{
  "project": "<PROJECT>",
  "aspects": ["file_tree"]
}')
```

### Scope architecture to a subdirectory
```bash
codebase-memory-mcp cli get_architecture --args-file <(echo '{
  "project": "<PROJECT>",
  "path": "sharedUI"
}')
```

---

## 3. Search (functions, classes, symbols)

### Natural-language / keyword search (BM25 full-text)
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --query "update user"
```

### Regex name pattern
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --name-pattern ".*ViewModel.*"

# Linked imports — find all callers of a function
codebase-memory-mcp cli search_graph --project <PROJECT> --name-pattern ".*ViewModel.*" --include-connected true
```

### Filter by file path
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --query "safeCall" --file-pattern "*.kt"
```

### Paginate search results
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --query "user" --limit 50 --offset 50
```

### Code search (graph-augmented grep over indexed files)
```bash
codebase-memory-mcp cli search_code --project <PROJECT> --pattern "safeCall"
```

### Find by qualified name pattern
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --qn-pattern ".*BaseViewModel.*"
```

---

## 4. Call graph & tracing

### Who calls a function? (inbound — callers)
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name setState --direction inbound --depth 3
```

### What does a function call? (outbound — callees)
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name sendMessage --direction outbound --depth 3
```

### Both directions
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name getConfig --direction both --depth 3
```

### Data-flow trace (argument propagation)
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name toDomain --mode data_flow
```

### Cross-service trace (HTTP/async routes)
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name safeCall --mode cross_service
```

### Include test files in the trace
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name setState --direction inbound --depth 3 --include-tests true
```

---

## 5. Code snippets (source of a function/class)

### Get source code by qualified name
```bash
codebase-memory-mcp cli get_code_snippet --project <PROJECT> --qualified-name "ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel.BaseViewModel.setState"
```

### Include neighbor symbols
```bash
codebase-memory-mcp cli get_code_snippet --project <PROJECT> --qualified-name "BaseViewModel" --include-neighbors true
```

Tip: Use `search_graph` first to find the exact `qualified_name`, then call `get_code_snippet` with it.

---

## 6. Change impact analysis (uncommitted / branch diff)

### Detect changes and map to affected symbols
```bash
codebase-memory-mcp cli detect_changes --project <PROJECT>

# Since a specific git ref
codebase-memory-mcp cli detect_changes --project <PROJECT> --since "HEAD~10"

# Against a base branch other than main
codebase-memory-mcp cli detect_changes --project <PROJECT> --base-branch develop

# Limit scope to a subdirectory
codebase-memory-mcp cli detect_changes --project <PROJECT> --scope "feature/chat"

# Control traversal depth
codebase-memory-mcp cli detect_changes --project <PROJECT> --depth 5
```

---

## 7. Dead code detection (Cypher queries)

### Functions with zero callers (excluding entry points)
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function) WHERE NOT ()-[:CALLS]->(f) AND NOT f.is_entry_point RETURN f.qualified_name, f.file_path"
```

### Classes that nothing inherits from
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (c:Class) WHERE NOT ()-[:INHERITS]->(c) AND NOT ()-[:IMPLEMENTS]->(c) RETURN c.qualified_name"
```

### Most-called functions (top 20 by fan-in)
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function)<-[:CALLS]-(caller) RETURN f.qualified_name, count(caller) AS callers ORDER BY callers DESC LIMIT 20"
```

### Complex / hot-path functions (high cyclomatic complexity)
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function) WHERE f.complexity >= 10 OR f.transitive_loop_depth >= 3 RETURN f.qualified_name, f.complexity, f.transitive_loop_depth, f.linear_scan_in_loop ORDER BY f.complexity DESC"
```

### Limit results
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function)<-[:CALLS]-(caller) RETURN f.qualified_name, count(caller) AS callers ORDER BY callers DESC LIMIT 20" \
  --max-rows 50
```

---

## 8. Architecture Decision Records (ADR)

### Get current ADR
```bash
codebase-memory-mcp cli manage_adr --project <PROJECT> --mode get
```

### Get ADR sections
```bash
codebase-memory-mcp cli manage_adr --project <PROJECT> --mode sections
```

### Update ADR with content
```bash
codebase-memory-mcp cli manage_adr --args-file <(echo '{
  "project": "<PROJECT>",
  "mode": "update",
  "content": "# ADR-001: Use Koin for DI\n\n## Context\nWe need a DI framework for KMP.\n\n## Decision\nUse Koin with annotations.\n\n## Status\nAccepted"
}')
```

---

## 9. Cross-repo intelligence

### Index two repos, then link them
```bash
codebase-memory-mcp cli index_repository --repo-path ~/projects/backend
codebase-memory-mcp cli index_repository --repo-path ~/projects/frontend

# Match routes/channels across both
codebase-memory-mcp cli index_repository --repo-path ~/projects/frontend --mode cross-repo-intelligence --target-projects '["*"]'

# Or target specific projects
codebase-memory-mcp cli index_repository --repo-path ~/projects/frontend --mode cross-repo-intelligence --target-projects '["backend-project-name"]'
```

---

## 10. Schema & diagnostics

### Get graph schema (node labels, edge types)
```bash
codebase-memory-mcp cli get_graph_schema --project <PROJECT>
```

### Ingest runtime traces (enhance graph with real call frequencies)
```bash
codebase-memory-mcp cli ingest_traces --args-file <(echo '{
  "project": "<PROJECT>",
  "traces": [
    {"caller": "ir.ai.speak.ChatViewModel.sendMessage", "callee": "ir.ai.network.safeCall", "count": 42}
  ]
}')
```

---

## 11. Configuration

```bash
# List all config
codebase-memory-mcp config list

# Get a specific value
codebase-memory-mcp config get auto_index

# Set a value
codebase-memory-mcp config set auto_index true
codebase-memory-mcp config set auto_index_limit 50000
codebase-memory-mcp config set auto_watch true

# Reset to defaults
codebase-memory-mcp config reset
```

---

## 12. 3D Graph visualization UI

### Install the UI variant
```bash
curl -fsSL https://raw.githubusercontent.com/DeusData/codebase-memory-mcp/main/install.sh | bash -s -- --ui --dir ~/.local/bin --skip-config
```

### Enable & launch
```bash
codebase-memory-mcp --ui=true --port=9749
# Then open http://localhost:9749 in your browser
```

### Disable
```bash
codebase-memory-mcp --ui=false
```

---

## 13. Update & uninstall

```bash
# Update to latest release
codebase-memory-mcp update

# Uninstall (removes binary; graph indexes listed for confirmation)
codebase-memory-mcp uninstall
```

---

## 14. Piped stdin syntax (alternative to flags)

All `cli` commands accept JSON via stdin instead of flags:

```bash
echo '{"project":"<PROJECT>","function_name":"setState","direction":"inbound","depth":3}' | codebase-memory-mcp cli trace_path
```

This is useful when the argument JSON is large or generated by another tool (e.g. `jq`).

---

## 15. Common workflows

### "What will break if I modify this file / function?"
```bash
codebase-memory-mcp cli detect_changes --project <PROJECT>
# Or, for a specific function:
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name setState --direction inbound --depth 4
```

### "Give me a mental model of a new repo"
```bash
codebase-memory-mcp cli index_repository --repo-path . --mode fast
codebase-memory-mcp cli get_architecture --project <PROJECT>    # read overview, hotspots, clusters, boundaries
```

### "Find the definition of X"
```bash
codebase-memory-mcp cli search_graph --project <PROJECT> --query "BaseViewModel"
# Take the qualified_name from the result, then:
codebase-memory-mcp cli get_code_snippet --project <PROJECT> --qualified-name "<qualified_name from above>"
```

### "Who are all the callers of this function, recursively?"
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name safeCall --direction inbound --depth 5
```

### "Show me the most complex / risky functions"
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function) WHERE f.complexity >= 10 OR f.transitive_loop_depth >= 3 RETURN f.qualified_name, f.complexity, f.transitive_loop_depth ORDER BY f.complexity DESC LIMIT 20"
```

### "Find dead code"
```bash
codebase-memory-mcp cli query_graph --project <PROJECT> --query \
  "MATCH (f:Function) WHERE NOT ()-[:CALLS]->(f) AND NOT f.is_entry_point RETURN f.qualified_name, f.file_path"
```

### "How do these two services communicate?"
```bash
codebase-memory-mcp cli trace_path --project <PROJECT> --function-name <handler> --mode cross_service
```

---

## Notes

- **Project name**: derived from the repo's absolute path with `/` replaced by `-`. To list them:
  ```bash
  codebase-memory-mcp cli list_projects
  ```
- **Indexing modes**: `fast` is recommended for the first pass (avoids parser crashes on edge-case files). Use `full` or `moderate` when you need similarity/semantic edges.
- **Performance**: sub-ms graph queries; indexing an average repo takes milliseconds to a few seconds. The Linux kernel (28M LOC) indexes in ~3 minutes.
- **Privacy**: 100% local. No telemetry. Your code never leaves your machine.
- **Cache location**: `~/.cache/codebase-memory-mcp/`
- **Logs**: `~/.cache/codebase-memory-mcp/logs/`
- **Token efficiency**: one graph query replaces dozens of grep/read cycles. On five structural queries: ~3,400 tokens vs ~412,000 via file-by-file exploration (99.2% reduction).
