---
title: 요구사항 설계 초안 (원본 요약)
category: source
status: reviewed
sources: [raw/요구사항설계초안.txt]
updated: 2026-09-26
---

# 요구사항 설계 초안 — 요약

원본: `raw/요구사항설계초안.txt`

## 요약
- **형태**: 단일 웹 페이지 앱(HTML/CSS/JS 한 파일)으로 설계됨. → 목표인 안드로이드 앱과 충돌했고, 네이티브로 결정됨 ([ADR 0001](../decisions/0001-native-android.md))
- **화면 4개**: 단어 목록 관리, 레벨 선택, 레벨1/2/3 학습(공통 레이아웃), 결과/진행 요약.
- 용어: 이 초안의 "레벨 1/2/3"(듣기/카드/쓰기)이 위키에서 말하는 **레벨**이다. 엑셀의 단어 묶음은 **레슨**이라고 부른다 ([ADR 0005](../decisions/0005-lesson-and-level-terms.md)). 한때 반대로 정리했던 [ADR 0002](../decisions/0002-level-vs-mode-terms.md)는 폐기되었다.
- **단어 데이터**: 엑셀 업로드(SheetJS), 필수 컬럼 `word`, `meaning`, 발음기호는 선택. 업로드 옵션은 교체/추가. → [단어 데이터](../product/word-data.md)
- **발음**: 브라우저 TTS(Web Speech API) 추천, 추후 녹음 mp3로 확장 가능성. → [발음·TTS](../tech/pronunciation-tts.md)
- **학습 레벨**: 1) 보기+듣기+뜻 자동 넘김, 2) 섞인 알파벳 카드 배열, 3) 키보드 입력. → [학습 레벨](../learning/learning-levels.md)
- **공통 요소**: 다시 듣기, 진행률(5/20), 정답률 집계.
- **저장**: 브라우저 저장소 → 기기 바꾸면 초기화, 내보내기/불러오기는 추후.
- **제작 순서**: UI 뼈대 → 엑셀 파싱 → 레벨1 → 레벨3 → 레벨2 → 통계.

## 원본에 남아 있던 확인 사항
- 엑셀 컬럼 고정 vs 헤더 자동 인식 → [Q-2](../open-questions.md#q-2)
- TTS 품질로 충분한지 vs 녹음 파일 → [Q-3](../open-questions.md#q-3)

## 반영된 페이지
[제품 개요](../product/overview.md) · [단어 데이터](../product/word-data.md) · [화면 구성](../product/screens.md) · [학습 레벨](../learning/learning-levels.md) · [발음·TTS](../tech/pronunciation-tts.md) · [저장소](../tech/storage.md)
