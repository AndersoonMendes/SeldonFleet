# Protocolo de classificação — Seldon Fleet

## 1. Objetivo

Classificar os dispositivos da frota em OK, ATENCAO, CRITICO ou INDETERMINADO usando métricas de consumo de bateria, falhas recentes e quadros perdidos.


## 2. Regra de referência

A classificação utiliza o consumo relativo de bateria por hora de tela ligada, a quantidade de falhas nos últimos 7 dias e a taxa de quadros perdidos (jank).

- **INDETERMINADO:** capacidade da bateria ausente ou menor/igual a zero; tempo de tela menor/igual a zero; ou consumo ausente, negativo ou não finito. Consumos em mWh sem tensão nominal válida também não podem ser normalizados.
- **CRÍTICO:** consumo relativo maior/igual a 12%/h, ou pelo menos 3 falhas nos últimos 7 dias, ou jank maior/igual a 15%.
- **ATENÇÃO:** consumo relativo maior/igual a 7%/h, ou pelo menos 1 falha nos últimos 7 dias, ou jank maior/igual a 8%.
- **OK:** nenhum dos critérios de Atenção ou Crítico é atendido.

A ordem de avaliação será: INDETERMINADO, CRÍTICO, ATENÇÃO e OK.

O consumo relativo é calculado dividindo o consumo em mAh pelo tempo de tela em horas e pela capacidade da bateria em mAh, multiplicando o resultado por 100.


## 3. Normalização de energia

Valores em mWh serão convertidos para mAh dividindo a energia pela tensão nominal. Valores já expressos em mAh não serão convertidos.

## 4. Dados incompletos

Registros sem os dados necessários para calcular o dreno relativo serão classificados como INDETERMINADO, sem divisão por zero ou geração de valores inválidos.

## 5. Ameaças à validade

O dreno relativo considera o consumo durante o tempo de tela ligada. Caso parte do consumo aconteça com a tela desligada, a métrica poderá não representar adequadamente o comportamento real do dispositivo.

## 6. Uso de inteligência artificial

As ferramentas de IA utilizadas, suas contribuições e as decisões tomadas pelo autor serão declaradas no README.md.
