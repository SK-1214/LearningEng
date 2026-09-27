# word_memory — LLM Wiki Schema

초등학교 1학년 자녀(여아)가 영어 단어를 외우도록 돕는 **안드로이드 학습 앱**을 만들기 위한 지식 베이스.
Karpathy의 LLM Wiki 패턴(https://gist.github.com/karpathy/442a6bf555914893e9891c11519de94f)을 따른다.

## 계층 구조

| 폴더 | 역할 | 규칙 |
|---|---|---|
| `raw/` | 원본 자료 (요구사항 메모, 단어 엑셀, 리서치 자료) | **사람이 넣고 LLM은 읽기만 한다. 절대 수정·삭제하지 않는다.** |
| `wiki/` | LLM이 작성·유지하는 마크다운 위키 | LLM이 전담. 사람은 읽고 코멘트한다. |
| `CLAUDE.md` | 이 문서 (스키마) | 사람과 LLM이 함께 개정한다. |

### raw/ 하위
- `raw/wordlists/` — 단어 리스트 엑셀(.xlsx)
- `raw/research/` — 학습법·기술 참고자료
- `raw/` 루트 — 요구사항 메모 등 기타

### wiki/ 하위
- `wiki/index.md` — 전체 페이지 목록 (검색의 출발점)
- `wiki/log.md` — 작업 기록 (append-only)
- `wiki/open-questions.md` — 미결정 사항 모음
- `wiki/sources/` — raw 자료 1건당 요약 페이지 1개
- `wiki/product/` — 사용자, 기능, 화면, 요구사항
- `wiki/learning/` — 학습 설계 (학습 레벨, 반복 학습, 동기부여)
- `wiki/tech/` — 안드로이드 기술 선택, 데이터 모델, 아키텍처
- `wiki/decisions/` — 확정된 설계 결정 (ADR), `NNNN-제목.md`

## 페이지 규칙

- 파일명: 영문 kebab-case (`learning-levels.md`). 본문은 한국어.
- 모든 페이지 상단에 frontmatter:
  ```yaml
  ---
  title: 페이지 제목
  category: product | learning | tech | decision | source | meta
  status: draft | reviewed | decided | superseded
  sources: [raw/파일명]      # 근거가 된 원본
  updated: YYYY-MM-DD
  ---
  ```
- 다른 페이지는 상대경로 마크다운 링크로 참조: `[학습 레벨](../learning/learning-levels.md)`
- 원본 근거가 있는 주장에는 출처를 단다: `(출처: raw/요구사항설계초안.txt)`
- 추측·제안은 "제안:"으로, 미결정은 `open-questions.md`에 `Q-번호`로 등록하고 본문에서 링크한다.
- 결정이 확정되면 `decisions/`에 ADR을 만들고 관련 페이지 status를 `decided`로 바꾼다.

## 작업 절차

### Ingest (raw에 새 자료가 들어왔을 때)
1. 자료를 읽고 핵심 내용을 사용자와 짧게 논의한다.
2. `wiki/sources/`에 요약 페이지를 만든다.
3. 영향받는 product/learning/tech 페이지를 갱신하거나 새로 만든다.
4. 기존 내용과 충돌하면 덮어쓰지 말고 충돌을 명시하고 `open-questions.md`에 등록한다.
5. `index.md` 갱신, `log.md`에 `## [YYYY-MM-DD] ingest | 자료명` 추가.

### Query (질문에 답할 때)
1. `index.md`에서 시작해 관련 페이지를 읽고, 출처와 함께 답한다.
2. 재사용 가치가 있는 답은 위키 페이지로 저장하고 로그에 `query`로 기록한다.

### Lint (주기적 점검)
- 페이지 간 모순, 오래된 내용, 고아 페이지(인바운드 링크 없음), 끊어진 링크, 빠진 개념 페이지를 찾아 보고·수정한다.
- 로그에 `lint`로 기록한다.

## 프로젝트 고정 컨텍스트
- 주 사용자: 초등학교 1학년 여아 → 큰 터치 영역, 짧은 세션, 읽기 부담 최소화, 즉각적인 칭찬 피드백을 항상 고려한다.
- 관리자: 부모 → 단어 리스트(엑셀) 업로드, 진행 상황 확인.
- 플랫폼: **네이티브 안드로이드 (Kotlin + Jetpack Compose)** — `wiki/decisions/0001-native-android.md`. 그 밖의 기술 스택은 `wiki/tech/` 참고.
- 목표: 스펠링 비 대회 준비. 철자는 아이가 **키보드로 직접 입력**한다 — `wiki/decisions/0003-keyboard-spelling-input.md`.

## 앱 코드 (`android/`)
- Kotlin + Compose + Room. 빌드·설치 명령과 진행 상황은 `wiki/product/mvp-roadmap.md`에 있다.
- 툴체인은 `C:\Users\sk80l\dev\` (jdk-17, android-sdk, gradle)에 있다. Android Studio는 없다. `JAVA_HOME`을 지정해 `./gradlew`를 실행한다.
- 설계를 바꾸는 코드 변경을 하면 관련 wiki 페이지와 log.md도 함께 갱신한다.

## 용어 (반드시 지킬 것 — `wiki/decisions/0005-lesson-and-level-terms.md`)
- **레슨**: 단어 묶음. 엑셀 `Lesson` 열의 값 (`4-1` → 레슨 4의 1번 단어). 코드에서는 `Lesson`.
- **레벨**: 공부 단계 — 레벨 1 듣기 / 레벨 2 카드 / 레벨 3 쓰기 (원본 초안의 "레벨 1/2/3"과 같음). 코드에서는 enum `StudyMode`(LISTEN/CARD/TYPE)를 쓰고, `Level`이라는 식별자는 쓰지 않는다.
- ADR 0002(레벨 = 엑셀 Lesson)는 사용자 말을 잘못 해석한 폐기된 결정이다. 따르지 않는다.
