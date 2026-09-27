---
title: "ADR 0005: 용어 — 레슨(엑셀 Lesson)과 레벨(듣기/카드/쓰기)"
category: decision
status: decided
sources: [raw/요구사항설계초안.txt, raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# ADR 0005: 용어 — 레슨(엑셀 Lesson)과 레벨(듣기/카드/쓰기)

- **결정일**: 2026-09-26 (사용자 정정)
- **대체한 결정**: [ADR 0002](0002-level-vs-mode-terms.md) — 사용자 답변 "레슨은 레벨이야"를 반대로 해석했었다.

## 사용자 설명
> 엑셀의 레벨은 단어 레슨의 의미니까 수정해줘. 레벨은 지금 듣기/카드/쓰기를 의미야.

## 결정
| 용어 | 뜻 | 값 | 코드 이름 |
|---|---|---|---|
| **레슨** (Lesson) | 단어 묶음. 엑셀 `Lesson` 열의 앞 숫자 (`4-1` → 레슨 4의 1번 단어) | 레슨 3 ~ 12 (2026-09-27 기준) | `Lesson` |
| **레벨** (Level) | 공부하는 단계. 원본 초안의 "레벨 1/2/3"과 같다 | **레벨 1 듣기 · 레벨 2 카드 · 레벨 3 쓰기** | `StudyMode` (LISTEN / CARD / TYPE) |

- 앱 흐름: **레슨 선택 → 레벨 선택 → 학습**
- 코드에서는 enum 이름 `StudyMode`를 유지한다. `Level`이라는 이름을 코드에 쓰면 예전 의미(레슨)와 헷갈리기 때문이다. 화면에는 "레벨 1 듣기"처럼 표시한다.
- 엑셀 헤더는 `Lesson`이 기본이다. 부모가 만든 다른 파일을 위해 `Level`, `레벨` 헤더도 단어 묶음 열로 인식한다.

## 영향
- 앱: 화면 문구("레슨 4", "레벨 1 듣기", "레벨 3 쓰기"), 코드 식별자(`LevelEntity` → `LessonEntity` 등), DB 테이블 `levels`/`level_words` → `lessons`/`lesson_words`. **DB 버전을 2로 올렸고, v1(첫 태블릿 테스트 때만 존재)은 삭제한다.** 태블릿에서 엑셀을 한 번 다시 가져와야 한다.
- 위키: `learning/learning-modes.md` → [learning/learning-levels.md](../learning/learning-levels.md)로 되돌림. 모든 페이지의 용어를 이 표에 맞춤.
