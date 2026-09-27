---
title: 데이터 저장
category: tech
status: draft
sources: [raw/요구사항설계초안.txt, raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# 데이터 저장

## 저장 대상 (출처: raw/요구사항설계초안.txt)
- 단어 목록
- 학습 진행 상황
- 레슨별·레벨별 정답률 (초안의 "레벨별"은 듣기/카드/쓰기별을 뜻함 — [ADR 0005](../decisions/0005-lesson-and-level-terms.md))

## 원본 초안
브라우저 저장소 사용 → 기기·브라우저를 바꾸면 초기화. 필요하면 나중에 내보내기/불러오기 추가.

## 안드로이드로 바꿀 때 (제안)
- 단어·학습 기록: **Room**(SQLite) — 단어별 정답/오답 이력을 쌓아 두면 복습·통계에 쓸 수 있다.
- 설정값(TTS 속도 등): **DataStore**
- 기기를 바꿀 때를 대비해 백업(내보내기/불러오기) 기능은 추후 검토.

## 데이터 모델 초안 (제안)
근거: [단어 리스트 요약](../sources/wordlist-spelling-bee.md) — `Lesson` 열(= 레슨), 레슨을 넘는 중복 단어 3개. 용어: [ADR 0005](../decisions/0005-lesson-and-level-terms.md)

```
Word          (id, text UNIQUE, meaning)            -- 고유 단어. 학습 기록의 기준
Lesson        (id, name, sortOrder)                 -- 엑셀 Lesson의 앞 숫자: "4", "5", ...
LessonWord    (lessonId, position, wordId, meaning) -- 레슨별 등장. 같은 단어라도 레슨마다 뜻이 다를 수 있어 meaning을 둠
Attempt       (id, wordId, mode, correct, answer, timestamp) -- 문제 풀이 기록. mode = 레벨: LISTEN(1) | CARD(2) | TYPE(3)
```
- 정답률과 복습 대상은 `Attempt`를 `wordId` 기준으로 모아서 계산 → `buy`를 레슨 6과 8 중 어디서 풀든 기록이 합쳐진다.
- 교체 업로드: Lesson과 LessonWord를 비우고 다시 넣는다. Word와 Attempt(기록)는 유지할지 여부를 정해야 한다. → [Q-4](../open-questions.md#q-4)
- 추가 업로드: 같은 레슨 이름이 이미 있으면 그 레슨을 덮어쓸지 합칠지 정해야 한다.

관련: [안드로이드 기술 스택](android-stack.md) · [단어 데이터](../product/word-data.md)

## DB 버전 기록
| 버전 | 날짜 | 내용 |
|---|---|---|
| 1 | 2026-09-26 | 첫 태블릿 테스트. 레슨 테이블 이름이 `levels`/`level_words`였다 |
| 2 | 2026-09-26 | 용어 정정([ADR 0005](../decisions/0005-lesson-and-level-terms.md))으로 `lessons`/`lesson_words`로 이름을 바꿨다. v1은 실사용 기록이 없어 삭제(`fallbackToDestructiveMigrationFrom(1)`)한다. **v3부터는 반드시 Migration을 작성한다** |
