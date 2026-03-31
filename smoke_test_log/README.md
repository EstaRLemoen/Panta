# Smoke Test Notes

This directory stores per-run smoke test logs and backups.

[Latest Edit in 2026-03-30 20:49]

## Naming

- A run directory name may be chosen manually or generated from a timestamp.
- Preferred default format: `YYYY-MM-DD_HH:MM:SS_label/`.
- If a human gives a log folder name, keep that name and add a timestamp prefix.
- If an AI creates the folder on its own, it should first check the existing naming style in `smoke_test_log/` and follow it when possible, or ask your user.

Examples:

- `2026-03-30_01:00:00_CSVParser/`
- `2026-03-30_01:00:00_null-arg-smoke/`

## information

- A file named `info.md`.
- You can write short summary about timestamp, what is the smoke test for, and how about the result.
- Beside, if you're human, you can also note some thoughts or even complain. It doesn't matter.
- If you're AI, note that you are AI.
- For the file naming and another rule INSIDE its own directory，the introduction in `info.md` is more reliable. This README is for default situation.

## Standard Command

Use this command for a normal Panta smoke run:

```bash
set -a && source cfg_snapshot_test/local.secrets.env && set +a && conda run --no-capture-output -n panta-env python -m panta.main 2>&1 | tee smoke_test_log/<run_dir>/runtime.log
```

Notes:

- This command loads local secrets for this run only.
- This command uses the `panta-env` conda environment without requiring a separate `conda activate` step.
- Run it from the repository root.
- `2>&1` ensures both stdout and stderr are captured.
- `tee` lets you watch the output live while also saving the full terminal log to `runtime.log`.
- Before each smoke run, set a unique `report_filepath` value in `src/panta/config.ini` so the generated HTML report file name is unique for that run.
- Important: `report_filepath` is not used as a full path. Panta builds the final report file name by joining:
  - source file base name
  - prompt type
  - `report_filepath`
- For example, if `source_code_file` is `CSVParser.java`, `prompt_type = control`, and `report_filepath = overnight_r1.html`, the final report file name becomes something like `CSVParser_control_overnight_r1.html` under `result-files/<report_label>/`.
- Because of that, `report_filepath` should be treated as a unique file-name suffix, not as a directory path.
- Do not set it to values like `smoke_test_log/foo/report.html` or `/tmp/report.html`, because those strings will be embedded into the generated file name instead of being used as an output directory.
- If you run multiple experiments from the same baseline, do not reuse the default `report_filepath = test_results.html`, or later runs may overwrite earlier reports with the same generated name.

## Runtime Log

- Save the terminal output of each smoke run as `runtime.log` inside that run directory.
- If the run uses a known command, also save it as `command_log.txt`.
- If available, record the matching CFG snapshot run directory in `info.md`
- If an older run already stores the snapshot path in `backup/cfg_snapshot_path.txt`, keep it there and do not rewrite that older run just for naming consistency.

## Input or Output or Snapshot or Other you want to Save

Generation work based on original test file if it actually exist, and will create or modify test file. Also there are other file need to be saved. So we need to save some file.

- `backup/XXX_before.java` is the test file the work based on.
- `backup/XXX_after.java` is the test file the work generated or the modified one.
- `backup/config.ini.snapshot`: the `src/panta/config.ini` used for the run. 
    - Be careful, do not make it to be "the backup of origin config before we modify the `config.ini` for our smoke". That should be named `original_config.ini.backup`, and we usually do not save it in `smoke_test_log/`.
- If you changed `report_filepath` for this run, keep that value in `backup/config.ini.snapshot` so the matching HTML report name can be traced later.
- If there are more file need to be save, human can do what you like and AI should ask the user about that, but remember to introduce what did you save in the `info.md`. AI could put them in `backup/etra` by default.
