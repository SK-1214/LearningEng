---
title: Wiki Index
category: meta
status: reviewed
sources: []
updated: 2026-09-26
---

# word_memory Wiki Index

초1 자녀용 안드로이드 영어 단어 학습 앱 지식 베이스. 규칙은 [CLAUDE.md](../CLAUDE.md), 작업 기록은 [log.md](log.md).

## Meta
- [미결정 사항](open-questions.md) — Q-1·Q-8·Q-11 ✅, Q-9 일부 해결; 남은 질문: Q-2~7(MVP 기본값 있음), Q-9(대회 날짜), Q-10, Q-12(기기)

## Sources
- [요구사항 설계 초안](sources/requirements-draft.md) — 웹 앱 기준 초안: 화면 4개, 레벨 3개(듣기/카드/쓰기), 엑셀 업로드, TTS
- [단어 리스트: spelling bee contest](sources/wordlist-spelling-bee.md) — 레슨(Lesson) 3~12 × 10단어 = 100행(고유 96개), 헤더 Lesson/word/meaning, 전체 단어 목록, 변경 이력

## Product
- [제품 개요](product/overview.md) — 목적, 사용자(자녀·부모), 핵심 기능
- [화면 구성](product/screens.md) — 관리 / 레슨 선택 / 레벨 선택 / 학습 / 결과
- [단어 데이터](product/word-data.md) — 실제 엑셀 형식, 파싱 규칙, 교체·추가 업로드
- [MVP 로드맵](product/mvp-roadmap.md) — 첫 버전 범위, 미결정 질문의 MVP 기본값, 개발 단계 M0~M5

## Learning
- [학습 레벨 설계](learning/learning-levels.md) — 레벨 1 듣기 · 2 카드 · 3 쓰기(최우선), 대회 연습, 긴 단어·띄어쓰기·반복 글자 처리

## Tech
- [안드로이드 기술 스택](tech/android-stack.md) — Kotlin + Compose 확정, xlsx 직접 파싱 제안
- [발음·TTS](tech/pronunciation-tts.md) — Android TextToSpeech, 동형이의어(read, wind) 주의
- [데이터 저장](tech/storage.md) — Room + DataStore, 데이터 모델(Word/Lesson/LessonWord/Attempt), DB 버전 기록

## Decisions
- [ADR 0001 네이티브 안드로이드](decisions/0001-native-android.md) — Kotlin + Jetpack Compose
- ~~[ADR 0002 용어: 레벨 vs 학습 모드](decisions/0002-level-vs-mode-terms.md)~~ — ⛔ 폐기, ADR 0005로 대체
- [ADR 0003 키보드 직접 입력](decisions/0003-keyboard-spelling-input.md) — 철자는 아이가 키보드로 입력, 레벨 3 쓰기 최우선
- [ADR 0004 기본 키보드](decisions/0004-system-keyboard.md) — 안드로이드 기본 키보드, 자동완성·추천 끄기 체크리스트
- [ADR 0005 용어: 레슨과 레벨](decisions/0005-lesson-and-level-terms.md) — 레슨 = 엑셀 Lesson(단어 묶음), 레벨 = 1 듣기 / 2 카드 / 3 쓰기
