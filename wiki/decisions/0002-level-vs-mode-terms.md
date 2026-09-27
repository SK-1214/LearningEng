---
title: "ADR 0002: 용어 — 레벨(엑셀 Lesson)과 학습 모드"
category: decision
status: superseded
sources: [raw/요구사항설계초안.txt, raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# ADR 0002: 용어 — 레벨(엑셀 Lesson)과 학습 모드

> ⛔ **폐기 (2026-09-26)** — 사용자 답변을 반대로 해석한 결정이다. [ADR 0005](0005-lesson-and-level-terms.md)로 대체되었다. 레슨 = 엑셀 Lesson, 레벨 = 듣기/카드/쓰기.

- **결정일**: 2026-09-26 (사용자 답변: "레슨은 레벨이야")

## 배경
- 단어 엑셀의 `Lesson` 열(4~12)과 원본 초안의 "레벨 1/2/3"(학습 방식)이 모두 "레벨"로 불릴 수 있어 혼동된다.
- 사용자 설명: 엑셀의 **Lesson = 레벨**이다.

## 결정
| 용어 | 뜻 | 값 |
|---|---|---|
| **레벨** (Level) | 단어 묶음의 단계. 엑셀 `Lesson` 열의 앞 숫자 | 4 ~ 12 (현재) |
| **학습 모드** (Mode) | 같은 단어를 공부하는 방식. 원본 초안의 "레벨 1/2/3" | 듣기 모드 · 카드 모드 · 쓰기 모드 |

- 엑셀 헤더 이름은 `Lesson` 그대로 두고, 앱 화면과 위키에서는 "레벨"이라고 부른다.
- 앱 흐름: **레벨 선택 → 학습 모드 선택 → 학습**
- 위키와 코드에서 초안의 "레벨 1/2/3"이라는 표현은 쓰지 않는다.

## 영향
- `learning/learning-levels.md` → `learning/learning-modes.md`로 이름 변경 (ADR 0005에서 다시 `learning-levels.md`로 되돌림)
- 데이터 모델의 `Lesson` 테이블 → `Level` → [데이터 저장](../tech/storage.md)
- 확인 필요: 해석이 맞는지 사용자 재확인 (이 ADR 작성 시점)
