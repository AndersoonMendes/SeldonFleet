# Protocolo de classificação — Seldon Fleet

## 1. Objetivo

Classificar os dispositivos da frota em OK, ATENCAO, CRITICO ou INDETERMINADO usando métricas de consumo de bateria, falhas recentes e quadros perdidos.

## 2. Regra de referência

A classificação seguirá os limiares da especificação:

- INDETERMINADO: capacidade da bateria menor ou igual a zero ou ausente, tempo de tela menor ou igual a zero ou unidade mWh sem tensão nominal.
- CRITICO: dreno relativo maior ou igual a 12 %/h, pelo menos 3 falhas recentes ou pelo menos 15% de quadros perdidos.
- ATENCAO: dreno relativo maior ou igual a 7 %/h, pelo menos 1 falha recente ou pelo menos 8% de quadros perdidos.
- OK: nenhum dos critérios anteriores é atendido.

A ordem de avaliação será: INDETERMINADO, CRITICO, ATENCAO e OK.

## 3. Normalização de energia

Valores em mWh serão convertidos para mAh dividindo a energia pela tensão nominal. Valores já expressos em mAh não serão convertidos.

## 4. Dados incompletos

Registros sem os dados necessários para calcular o dreno relativo serão classificados como INDETERMINADO, sem divisão por zero ou geração de valores inválidos.

## 5. Ameaças à validade

O dreno relativo considera o consumo durante o tempo de tela ligada. Caso parte do consumo aconteça com a tela desligada, a métrica poderá não representar adequadamente o comportamento real do dispositivo.

## 6. Uso de inteligência artificial

As ferramentas de IA utilizadas, suas contribuições e as decisões tomadas pelo autor serão declaradas no README.md.
