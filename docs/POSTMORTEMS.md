# Neural Engine Post-Mortems

## Auto-Generated Lessons & Negative Constraints

### ❌ [2026-09-10] .codespellrc `source: mutation-cycle`
**Symptom:** AST / TypeScript Compiler Validation Rejected
**EVIDENCE (Machine-Copied Fact):**
```
Line 100, Col 1: Unclosed multiline comment block (/* ... */).
Line 77, Col 72: Unclosed opening delimiter '('.
```
**CONSTRAINT (Model Generalization):** Never repeat code patterns that produce this compiler/linter error on .codespellrc.

### ❌ [2026-09-10] .github/ISSUE_TEMPLATE/external-plugin.yml `source: mutation-cycle`
**Symptom:** AST / TypeScript Compiler Validation Rejected
**EVIDENCE (Machine-Copied Fact):**
```
Line 134, Col 57: Unclosed single-quote string literal.
```
**CONSTRAINT (Model Generalization):** Never repeat code patterns that produce this compiler/linter error on .github/ISSUE_TEMPLATE/external-plugin.yml.

### ❌ [2026-09-10] .github/workflows/agentics-maintenance.yml `source: mutation-cycle`
**Symptom:** AST / TypeScript Compiler Validation Rejected
**EVIDENCE (Machine-Copied Fact):**
```
Line 6, Col 23: Unclosed single-quote string literal.
Line 13, Col 17: Unclosed single-quote string literal.
Line 14, Col 24: Unclosed opening delimiter '('.
Line 7, Col 35: Unclosed opening delimiter '('.
Line 7, Col 12: Unclosed opening delimiter '('.
```
**CONSTRAINT (Model Generalization):** Never repeat code patterns that produce this compiler/linter error on .github/workflows/agentics-maintenance.yml.
