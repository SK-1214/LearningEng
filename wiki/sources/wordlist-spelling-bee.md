---
title: 단어 리스트 — spelling bee contest.xlsx (원본 요약)
category: source
status: reviewed
sources: [raw/wordlists/spelling bee contest.xlsx]
updated: 2026-09-27
---

# 단어 리스트 — spelling bee contest.xlsx

원본: `raw/wordlists/spelling bee contest.xlsx`

## 구조
- 시트 1개(`Sheet1`), 범위 A1:C101, 병합 셀 없음 (2026-09-27 개정본)
- 헤더: **`Lesson` | `word` | `meaning`**
- `Lesson` 값 형식: `"레슨-번호"` 문자열 (예: `4-1`, `12-10`). **Lesson = 레슨** (단어 묶음, [ADR 0005](../decisions/0005-lesson-and-level-terms.md)).
- 발음기호, 예문, 이미지 열 없음

## 통계 (2026-09-27 개정본)
- **100행 = 레슨 3~12 (10개) × 10단어**. 레슨 1~2는 없음 → [Q-10](../open-questions.md#q-10)
  - 레슨 3의 10행은 시트 **맨 아래(92~101행)**에 추가되었다. 앱은 레슨 번호순으로 정렬하므로 레슨 3이 맨 앞에 나온다.
- 고유 단어 **96개**. 레슨을 넘어 중복되는 단어 4개:
  - `buy` (6-6, 8-5), `run` (10-9, 12-6), `together` (10-7, 12-9), **`should` (3-9 "~을 해야한다", 10-8 "~해야 한다")** — 뜻 표기가 레슨마다 다르다. 레슨별 뜻은 각각 유지되고, 대회 연습과 틀린 단어 모음에서는 나중 행(3-9)의 뜻이 쓰인다([데이터 저장](../tech/storage.md)).
- 띄어쓰기가 있는 단어 2개: `take off` (4-2), `movie star` (8-10)
- 모두 소문자이며 대문자는 없음
- 글자 수 분포 (공백 제외):

| 글자 수 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|
| 단어 수 | 17 | 36 | 19 | 9 | 6 | 5 | 4 | 4 |

  → 3~5글자가 72%. 가장 긴 단어는 `sunglasses`, `skateboard`, `basketball`, `summertime` (10글자). 레슨 3의 `zookeeper`, `sunscreen`, `summertime`은 8글자 이상이라 레벨 2 카드에서 첫 글자 힌트가 나온다.

## 특징
- 파일명으로 보아 **스펠링 비 대회 준비용**이다. 철자는 키보드로 직접 입력한다 ([ADR 0003](../decisions/0003-keyboard-spelling-input.md)). 대회 일정 → [Q-9](../open-questions.md#q-9)
- 뜻이 사전적 기본 의미가 아니라 **이야기 문맥에 맞춘 뜻**인 경우가 있다. 교재 스토리 기반으로 보인다.
  - 예: `sweet`=향기로운, `hard`=세게, `air`=공중, `trip`=발을 헛디디다, `look`=~처럼 보이다, `stitch`=바늘땀
- 뜻에 `~`, 쉼표가 들어간 경우: `조금, 약간의`, `줄, 선`, `~처럼 보이다`, `~해야 한다`, `~을 해야한다`, `입다, 바르다`, `~도`

## 변경 이력
| 날짜 | 변경 |
|---|---|
| 2026-09-26 | 첫 버전: 레슨 4~12, 90행, 고유 87개 |
| 2026-09-26 | 9-3 `line` 뜻 "선글라스" → "줄, 선" |
| 2026-09-27 | **레슨 3 추가**(10단어, 92~101행). 10-8 `should` 뜻 "~해야한다" → "~해야 한다" |

## 데이터 오류 (해결됨)
- 9-3 `line`의 뜻이 "선글라스"로 잘못 들어가 있었다(8-4 `sunglasses`에서 복사된 것으로 보임). → [Q-8](../open-questions.md#q-8)
- ✅ 2026-09-26: 사용자가 raw 엑셀을 **"줄, 선"** 으로 고쳤고, 다시 읽어서 확인했다(파일 수정 시각 2026-09-26 22:00). 다른 행은 바뀌지 않았다.

## 단어 목록 (레슨별)
| 레슨 | 단어 (뜻) |
|---|---|
| 3 | zookeeper(사육사), drink(마시다), soda(탄산음료), sunscreen(선크림), rainy(비가 오는), storm(폭풍우), summertime(여름철), zoo(동물원), should(~을 해야한다), wear(입다, 바르다) |
| 4 | strong(강한), take off(벗다), cold(추운), shine(빛나다), sun(태양), win(이기다), wind(바람), blow(불다), hard(세게), feel(느끼다) |
| 5 | clothes(옷), shirt(셔츠), sweater(스웨터), dress(원피스), new(새로운), market(시장), want(원하다), need(필요하다), nice(멋진), pretty(예쁜) |
| 6 | groceries(식료품류), bread(빵), juice(주스), jam(잼), heavy(무거운), buy(사다), some(조금, 약간의), get(얻다), basket(바구니), food(음식) |
| 7 | flower(꽃), sweet(향기로운), fresh(신선한), seed(씨앗), plant(심다), smell(냄새가 나다), colorful(형형색색의), rose(장미), tulip(튤립), garden(정원) |
| 8 | sell(팔다), ride(타다), coat(외투), sunglasses(선글라스), buy(사다), skateboard(스케이트보드), look(~처럼 보이다), warm(따뜻한), big(큰), movie star(영화배우) |
| 9 | stitch(바늘땀), baseball(야구공), line(줄, 선), basketball(농구공), hole(구멍), part(부분), ball(공), guess(추측하다), right(맞는), know(알다) |
| 10 | pool(수영장), rule(규칙), stretch(뻗다), dive(다이빙하다), push(밀다), read(읽다), together(함께), should(~해야 한다), run(달리다), teacher(선생님) |
| 11 | arm(팔), slide(미끄러지다), swan(백조), spin(돌다), tornado(회오리바람), dance(춤추다), ice(얼음), music(음악), leg(다리), air(공중) |
| 12 | race(경주), trip(발을 헛디디다), fall(넘어지다), hug(껴안다), clap(손뼉을 치다), run(달리다), too(~도), help(돕다), together(함께), everyone(모든 사람) |

## 반영된 페이지
[단어 데이터](../product/word-data.md) · [학습 레벨](../learning/learning-levels.md) · [데이터 저장](../tech/storage.md) · [발음·TTS](../tech/pronunciation-tts.md) · [제품 개요](../product/overview.md)
