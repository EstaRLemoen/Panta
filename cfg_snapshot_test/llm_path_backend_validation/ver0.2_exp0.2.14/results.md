# ver0.2_exp0.2.14 results

- llm final coverage: `47.52% / 30.36%`
- comex final coverage: `61.39% / 33.93%`
- llm selected-path snapshots show `_llm_direct_path_text` attached and consumed, but selected examples in this smoke mostly sanitize/fall back to analyzer-derived text.
- comex selected-path snapshots keep `direct_path_text` and `path_prompt_text` empty, so comex prompt text remains unchanged.
