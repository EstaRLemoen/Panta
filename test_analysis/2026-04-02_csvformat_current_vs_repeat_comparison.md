# CSVFormat Current-vs-Repeat Comparison

Date: 2026-04-02

Scope:
- Previous repeat run: `repeat-csvformat-25iter`
- Current-template run: `csvformat-current-light-advice-25iter`

This note records only the key factual comparison between the two two-round CSVFormat experiments.

## Run Groups

### Previous repeat run

- Log: `offline_runs/repeat_csvformat_25iter_2rounds.log`
- R1 run dir: `smoke_test_log/2026-04-02_02:13:37_repeat-csvformat-25iter_r1_CSVFormat`
- R2 run dir: `smoke_test_log/2026-04-02_03:06:16_repeat-csvformat-25iter_r2_CSVFormat`

### Current-template run

- Log: `offline_runs/run_csvformat_current_light_advice_25iter_2rounds.log`
- R1 run dir: `smoke_test_log/2026-04-02_17:16:52_csvformat-current-light-advice-25iter_r1_CSVFormat`
- R2 run dir: `smoke_test_log/2026-04-02_18:28:01_csvformat-current-light-advice-25iter_r2_CSVFormat`

## Final Coverage Comparison

| Group | Round | Final line coverage | Final branch coverage | Stop point |
|---|---|---:|---:|---|
| Previous repeat | R1 | `42.05%` | `27.2%` | Iteration 12 |
| Previous repeat | R2 | `50.0%` | `41.38%` | Iteration 16 |
| Current template | R1 | `64.36%` | `55.94%` | Iteration 16 |
| Current template | R2 | `27.95%` | `13.79%` | Iteration 3 |

## Direct Delta Table

| Comparison | Line delta | Branch delta |
|---|---:|---:|
| Current R1 vs Previous R1 | `+22.31%` | `+28.74%` |
| Current R2 vs Previous R2 | `-22.05%` | `-27.59%` |

## Advice Breadth Counts

### Previous repeat run

Based on `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_021352_22667006` and `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_030637_a4987b86`:

- R1 advice calls: `12`
- R2 advice calls: `16`
- Each inspected advice batch contained `4` designs

### Current-template run

Based on `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_171712_64c5a585` and `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_182822_bd54e391`:

- R1 advice calls: `16`
- R2 advice calls: `3`
- R1 design counts by advice call: `5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 5, 5, 5, 4`
- R2 design counts by advice call: `5, 5, 5`

## Coverage Trajectory Comparison

### Previous repeat run

| Iter | Prev R1 line | Prev R2 line |
|---|---:|---:|
| 0 | `27.95%` | `27.69%` |
| 1 | `30.77%` | `27.69%` |
| 2 | `40.77%` | `27.69%` |
| 3 | `40.77%` | `31.79%` |
| 4 | `40.77%` | `36.41%` |
| 5 | `41.03%` | `41.28%` |
| 6 | `41.54%` | `43.33%` |
| 7 | `41.54%` | `43.33%` |
| 8 | `41.79%` | `45.13%` |
| 9 | `42.05%` | `46.15%` |
| 10 | `42.05%` | `48.21%` |
| 11 | `42.05%` | `48.21%` |
| 12 | `42.05%` | `49.74%` |
| 13 | — | `49.74%` |
| 14 | — | `49.74%` |
| 15 | — | `50.0%` |
| 16 | — | `50.0%` |

### Current-template run

| Iter | Current R1 line | Current R2 line |
|---|---:|---:|
| 0 | `27.69%` | `27.95%` |
| 1 | `30.0%` | `27.95%` |
| 2 | `30.51%` | `27.95%` |
| 3 | `34.62%` | `27.95%` |
| 4 | `53.33%` | — |
| 5 | `56.15%` | — |
| 6 | `58.21%` | — |
| 7 | `59.74%` | — |
| 8 | `60.51%` | — |
| 9 | `61.28%` | — |
| 10 | `62.82%` | — |
| 11 | `63.59%` | — |
| 12 | `64.1%` | — |
| 13 | `64.36%` | — |
| 14 | `64.36%` | — |
| 15 | `64.36%` | — |
| 16 | `64.36%` | — |

## Per-Round Design And Passing-Test Summary

### Previous repeat R1

Source artifacts:

- Run dir: `smoke_test_log/2026-04-02_02:13:37_repeat-csvformat-25iter_r1_CSVFormat`
- Snapshot dir: `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_021352_22667006`

| Iter | Advice designs | Passing tests before coverage check |
|---|---|---|
| 0 | baseline only | `testNewFormatWithLineBreakDelimiter`, `testWithDelimiterLineBreak`, `testWithCommentMarkerLineBreak`, `testWithEscapeLineBreak`, `testWithQuoteLineBreak`, `testWithNullString` |
| 1 | header null; different delimiters; escaping in print; comment marker | `testWithHeaderNull`, `testEqualsDifferentDelimiters`, `testWithCommentMarker` |
| 2 | null header; different formats; comment marker; escaping | `testPrintAndEscape`, `testEqualsDifferentDelimitersUpdated`, `testWithCommentMarkerUpdated` |
| 3 | header null init; different formats; quote null; escape line break | `testWithQuoteNull`, `testWithHeaderNullUpdated`, `testWithEscapeLineBreakUpdated`, `testEqualsDifferentDelimitersUpdatedFixed` |
| 4 | header null; different delimiters; comment marker line break; escape equals delimiter | no passing test logged before coverage check |
| 5 | header null; different formats; quote null; escape null | `testWithHeaderNullBehavior`, `testEqualsDifferentCSVFormats`, `testWithEscapeNull`, `testWithQuoteNullFixed` |
| 6 | header null; null object; different class type; escape null | `testEqualsWithNullObject`, `testEqualsWithDifferentClassType`, `testWithHeaderNullBehaviorFixed`, `testWithEscapeNullFixed` |
| 7 | header null; different delimiters; quote null; escape null | no passing test logged before coverage check |
| 8 | header null init; different formats; escape line break; quote equals delimiter | `testWithHeaderNullInitialization`, `testWithQuoteSameAsDelimiter`, `testEqualsDifferentCSVFormatsUpdated` |
| 9 | header null init; different delimiters; quote null; escape equals delimiter | `testWithEscapeSameAsDelimiter`, `testWithHeaderNullInitializationFixed`, `testEqualsDifferentDelimitersFixedUpdated`, `testWithQuoteNullFixedUpdated` |
| 10 | header null init; different delimiters; escape null; quote null | no passing test logged before coverage check |
| 11 | header null init; different delimiters; quote null; escape line break | no passing test logged before coverage check |
| 12 | header null; different delimiter settings; quote null; escape line break | `testWithQuoteNullUpdated`, `testWithEscapeLineBreakFixed` |

### Previous repeat R2

Source artifacts:

- Run dir: `smoke_test_log/2026-04-02_03:06:16_repeat-csvformat-25iter_r2_CSVFormat`
- Snapshot dir: `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_030637_a4987b86`

| Iter | Advice designs | Passing tests before coverage check |
|---|---|---|
| 0 | baseline only | `testNewFormatWithLineBreakDelimiter`, `testWithLineBreakDelimiter`, `testWithLineBreakCommentMarker`, `testWithLineBreakEscapeCharacter`, `testWithLineBreakQuoteCharacter` |
| 1 | header null; different formats; escaping enabled but unset; unused comment marker | `testNewFormatWithNullHeader`, `testEqualsWithDifferentFormats`, `testIsCommentMarkerSetWithUnusedMarker` |
| 2 | header null; different delimiters; escape equals delimiter; quote equals delimiter | `testWithHeaderNull`, `testEqualsDifferentDelimiters`, `testWithEscapeSameAsDelimiter`, `testWithQuoteSameAsDelimiter` |
| 3 | equals; header cloning; null string in print; special-character escaping | `testHeaderCloningBehavior`, `testEqualsDifferentDelimitersFixed`, `testNullStringHandlingInPrintMethod` |
| 4 | equals; null string; escape not set; comment marker set | `testEqualsWithIdenticalFormats`, `testGetNullString`, `testIsEscapeCharacterSetFalse`, `testIsCommentMarkerSetTrue` |
| 5 | identical formats; different quote modes; null-value printing; empty header comments | `testEqualsWithIdenticalCSVFormats`, `testPrintWithNullValue`, `testGetHeaderCommentsWithEmptyComments`, `testEqualsWithDifferentQuoteModes` |
| 6 | different quote chars; null/non-null quote; different escape chars; different record separators | `testEqualsWithDifferentQuoteCharacters`, `testEqualsWithNullAndNonNullQuoteCharacters`, `testEqualsWithDifferentEscapeCharacters`, `testEqualsWithDifferentRecordSeparators` |
| 7 | different configurations; hashCode consistency; null-string printing; trimming | `testHashCodeConsistency`, `testPrintWithConfiguredNullString`, `testEqualsWithDifferentQuoteCharactersFixed` |
| 8 | different configurations; null string; quote handling; escape handling | `testEqualsWithDifferentDelimiters`, `testIsNullStringSetWhenNullStringIsNotSet`, `testIsQuoteCharacterSetWhenQuoteCharacterIsSet`, `testIsEscapeCharacterSetWhenEscapeCharacterIsSet`, `testWithSkipHeaderRecord` |
| 9 | different configurations; hashCode consistency; print null handling; skip header | `testEqualsWithDifferentDelimitersFixed`, `testHashCodeConsistencyFixed`, `testPrintHandlesNullValueFixed` |
| 10 | equality comparison; hashCode consistency; print with configs; trimming | `testWithTrimEnabled`, `testEqualsIdenticalCSVFormats`, `testEqualsDifferentCSVFormats`, `testPrintWithNullValueFixed` |
| 11 | null object; different class types; matching quote chars; different null strings | `testEqualsWithNullObject`, `testEqualsWithDifferentClassTypes`, `testEqualsWithMatchingQuoteCharacters`, `testEqualsWithDifferentNullStrings` |
| 12 | quote set; comment marker set; escape set; null string set | `testEqualsWithQuoteCharacterSet`, `testEqualsWithCommentMarkerSet`, `testEqualsWithEscapeCharacterSet`, `testEqualsWithNullStringSet` |
| 13 | different quote chars; null comment marker; different escape chars; different null strings | `testEqualsWithNullCommentMarker`, `testEqualsWithDifferentEscapeCharactersFixed`, `testEqualsWithDifferentNullStringsFixed` |
| 14 | quote set; comment marker set; escape set; null string set | `testEqualsWithQuoteCharacterSetFixed`, `testEqualsWithCommentMarkerSetFixed`, `testEqualsWithEscapeCharacterSetFixed`, `testEqualsWithNullStringSetFixed` |
| 15 | null string in CSV format; different escape chars; ignore empty lines; different record separators | `testEqualsWithNullStringSetToNull`, `testEqualsWithIgnoreEmptyLines`, `testEqualsWithDifferentRecordSeparatorsFixed` |
| 16 | quote set; comment marker set; escape set; null string set | `testEqualsWithDifferentQuoteCharactersSet`, `testEqualsWithDifferentEscapeCharactersSet`, `testEqualsWithDifferentNullStringsSet`, `testEqualsWithDifferentCommentMarkersSet` |

### Current-template R1

Source artifacts:

- Run dir: `smoke_test_log/2026-04-02_17:16:52_csvformat-current-light-advice-25iter_r1_CSVFormat`
- Snapshot dir: `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_171712_64c5a585`

| Iter | Advice designs | Passing tests before coverage check |
|---|---|---|
| 0 | baseline only | `testNewFormatWithLineBreakDelimiter`, `testWithLineBreakDelimiter`, `testWithLineBreakCommentMarker`, `testWithLineBreakEscapeCharacter`, `testWithLineBreakQuoteCharacter` |
| 1 | header null; different formats; print escape; quote null; skip header | `testNewFormatWithNullHeader`, `testEqualsWithDifferentFormats`, `testWithQuoteCharacterNull` |
| 2 | header null; different formats; escape line break; quote equals delimiter; null-string conflicts with delimiter | `testWithHeaderNull`, `testEqualsWithDifferentCSVFormats`, `testWithEscapeLineBreak`, `testWithQuoteSameAsDelimiter` |
| 3 | header null; different headers; quote null; escape equals delimiter; comment marker equals delimiter | `testEqualsWithDifferentHeaders`, `testWithEscapeSameAsDelimiter`, `testWithCommentMarkerSameAsDelimiter`, `testNewFormatWithNullHeaderFixed`, `testWithQuoteCharacterNullFixed` |
| 4 | identical formats; different formats; print null values; print special chars; comment marker set | `testEqualsIdenticalCSVFormats`, `testEqualsDifferentCSVFormats`, `testPrintNullValue`, `testPrintSpecialCharacters`, `testIsCommentMarkerSet` |
| 5 | identical objects; different quote modes; null string formatting; special-character escaping; skip header | `testEqualsIdenticalCSVFormatsWithSameSettings`, `testFormatWithNullValue`, `testGetSkipHeaderRecord`, `testEqualsDifferentQuoteModes` |
| 6 | null/different class types; different quote chars; identical formats; different escape chars; different null strings | `testEqualsWithNullArgument`, `testEqualsWithDifferentClassType`, `testEqualsWithDifferentQuoteCharacters`, `testEqualsWithDifferentEscapeCharacters`, `testEqualsWithDifferentNullStrings`, `testEqualsWithIdenticalInstances` |
| 7 | quote null in equals; comment marker null in equals; escape null in equals; nullString null in equals; ignoreSurroundingSpaces differs | `testEqualsWithNullQuoteCharacter`, `testEqualsWithNullCommentMarker`, `testEqualsWithNullEscapeCharacter`, `testEqualsWithNullNullString`, `testEqualsWithDifferentIgnoreSurroundingSpaces` |
| 8 | quote/comment/escape/nullString set but not used; ignore empty lines set | `testEqualsWithQuoteCharacterSetButNotUsed`, `testEqualsWithCommentMarkerSetButNotUsed`, `testEqualsWithEscapeCharacterSetButNotUsed`, `testEqualsWithNullStringSetButNotUsed`, `testEqualsWithIgnoreEmptyLinesFlagSet` |
| 9 | quote/comment/escape/nullString set but not used; record separator set but not used | `testIsQuoteCharacterSetWhenNotUsed`, `testIsCommentMarkerSetWhenNotUsed`, `testIsEscapeCharacterSetWhenNotUsed`, `testIsNullStringSetWhenNotUsed`, `testGetRecordSeparatorWhenNotUsed` |
| 10 | quote not used; escape not used; null string defined; header comments present; record separator set | `testIsQuoteCharacterSetWithQuoteCharacterSet`, `testIsEscapeCharacterSetWithEscapeCharacterSet`, `testIsNullStringSetWithNullStringDefined`, `testGetHeaderCommentsWithCommentsDefined`, `testGetRecordSeparatorWithRecordSeparatorDefined`, `testGetRecordSeparatorWithoutRecordSeparatorDefined` |
| 11 | quote null; escape null; null string null; skip header true; record separator null | `testEqualsWithNullStringSetToNull`, `testEqualsWithSkipHeaderRecordSetToTrue`, `testEqualsWithNullRecordSeparator`, `testEqualsWithNullQuoteCharacterFixed`, `testEqualsWithNullEscapeCharacterFixed` |
| 12 | quote used; escape used; null string used; header comments set and printed | `testFormatWithQuoteCharacter`, `testPrintWithNullString` |
| 13 | quote set; escape set; null string set; record separator set; header set | `testEqualsWithQuoteCharacterSet`, `testEqualsWithEscapeCharacterSet`, `testEqualsWithNullStringSet`, `testEqualsWithRecordSeparatorSet`, `testEqualsWithHeaderSet` |
| 14 | quote set; escape set; null string set; record separator set; header set | `testEqualsWithDifferentQuoteCharacter`, `testEqualsWithDifferentEscapeCharacter`, `testEqualsWithDifferentNullString`, `testEqualsWithDifferentRecordSeparator`, `testEqualsWithDifferentHeader` |
| 15 | quote not used; escape not used; null string defined; record separator set | passing tests present but no further coverage increase |
| 16 | no new advice beyond prior no-growth window | no further coverage increase |

### Current-template R2

Source artifacts:

- Run dir: `smoke_test_log/2026-04-02_18:28:01_csvformat-current-light-advice-25iter_r2_CSVFormat`
- Snapshot dir: `cfg_snapshot_test/intermediate/cfg/CSVFormat_20260402_182822_bd54e391`

| Iter | Advice designs | Passing tests before coverage check |
|---|---|---|
| 0 | baseline only | `testNewFormatWithLineBreakDelimiter`, `testWithDelimiterLineBreak`, `testWithCommentMarkerLineBreak`, `testWithEscapeLineBreak`, `testWithQuoteLineBreak`, `testWithNullString` |
| 1 | header null; different formats; quote null; special-character escaping; comment marker set | no passing test logged before coverage check |
| 2 | header null init; different delimiters; quote null; print with trailing delimiter; format with null values | no passing test logged before coverage check |
| 3 | header null init; different delimiters; quote equals delimiter; escape equals delimiter; comment marker equals delimiter | no passing test logged before coverage check |

## Minimal Factual Observations

1. The current-template run produced broader light-advice batches than the previous repeat run: mostly `5` designs instead of `4`.
2. The current-template R1 achieved the highest final coverage across these four CSVFormat rounds: `64.36% / 55.94%`.
3. The current-template R2 stopped after three consecutive no-growth iterations and remained at `27.95% / 13.79%`.
4. The previous repeat run showed lower final ceilings than current-template R1, but less severe spread between its two rounds.
5. The current-template two-round result therefore shows both a higher observed upside and a larger round-to-round variance.
