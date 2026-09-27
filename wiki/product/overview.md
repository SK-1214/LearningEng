---
title: 제품 개요
category: product
status: draft
sources: [raw/요구사항설계초안.txt, raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-26
---

# 제품 개요

## 목적
초등학교 1학년 자녀가 부모가 정해 준 영어 단어를 **듣고 → 보고 → 조합하고 → 직접 쓰는** 단계로 외우게 한다.

**현재 학습 목표**: 스펠링 비 대회 준비 — 레슨 3~12, 100행·고유 96단어(2026-09-27 기준) (출처: raw/wordlists/spelling bee contest.xlsx → [단어 리스트 요약](../sources/wordlist-spelling-bee.md)). 철자는 아이가 **키보드로 직접 입력**한다 ([ADR 0003](../decisions/0003-keyboard-spelling-input.md)). 대회 일정은 [Q-9](../open-questions.md#q-9)

**용어**: 레슨 = 엑셀 `Lesson`(단어 묶음), 레벨 = 레벨 1 듣기 / 레벨 2 카드 / 레벨 3 쓰기 ([ADR 0005](../decisions/0005-lesson-and-level-terms.md))

## 사용자
| 사용자 | 하는 일 | 설계 시 고려 |
|---|---|---|
| 자녀 (초1, 여아) | 레슨과 레벨을 골라 학습 | 한글 읽기가 막 익숙해지는 시기 → 글 대신 아이콘·음성 안내. 큰 버튼, 짧은 세션(5~10분), 틀려도 부정적이지 않은 피드백 |
| 부모 | 엑셀로 단어 등록, 진행률 확인 | 관리 화면은 아이가 실수로 들어가지 않게 분리 제안 [Q-5](../open-questions.md#q-5) |

## 핵심 기능
1. 엑셀 단어 리스트 업로드(교체/추가) → [단어 데이터](word-data.md)
2. 레슨(현재 3~12)별 학습 + 레벨 3단계(1 듣기 / 2 카드 / 3 쓰기, 레벨 3 최우선) → [학습 레벨](../learning/learning-levels.md)
3. 발음 재생 → [발음·TTS](../tech/pronunciation-tts.md)
4. 진행률·정답률 저장 및 요약 → [저장소](../tech/storage.md)

## 플랫폼
**네이티브 안드로이드 앱 (Kotlin + Jetpack Compose)** — [ADR 0001](../decisions/0001-native-android.md). 원본 초안의 웹 기술은 [안드로이드 기술 스택](../tech/android-stack.md)으로 바꾼다.

화면 목록: [화면 구성](screens.md) · 개발 순서: [MVP 로드맵](mvp-roadmap.md)
