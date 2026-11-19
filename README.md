# Territory Predictive Insights — Clima Local

**Global Solution 2º/2026 – POO (Turma de Fevereiro) — FIAP**
Tema: **CitizenTech** · Recorte: **chuva intensa e risco de alagamento em pontos vulneráveis**

Sistema de console em Java que lê sensores simulados (chuva, nível do rio e umidade do solo),
grava as medições em banco, faz previsões simples a partir do histórico e emite alertas quando
a previsão ultrapassa limiares configuráveis.

---

## 1. Integrantes

| Nome | RM |
|---|---|
| _(preencher)_ | _(preencher)_ |
| _(preencher)_ | _(preencher)_ |
| _(preencher)_ | _(preencher)_ |

---

## 2. Público beneficiado

**Quem sofre o problema:** moradores de áreas sujeitas a alagamento — margens de córregos,
passagens sob viadutos, encostas com solo encharcado — que normalmente só percebem o risco
quando a água já subiu.

**Quem usa o sistema:** a **defesa civil municipal** e/ou a **associação de bairro** que
acompanha esses pontos vulneráveis.

**Que decisão o sistema apoia:**

- acionar o **alerta comunitário** (mensagem para os moradores, sirene, aviso na associação);
- mandar uma equipe **desobstruir bueiros e bocas de lobo** antes do pico da chuva;
- **interditar** uma passagem que costuma alagar;
- **preparar um abrigo** temporário quando o nível do córrego tende a passar do limite crítico.

O território é descrito apenas por **região e equipamento** (ponto de monitoramento e sensor).
Nenhum dado pessoal é usado.

---

## 3. Como rodar

### Pré-requisitos

- **JDK 21** (ou superior) com `JAVA_HOME` configurado — o projeto compila para Java 21.
- **Maven 3.9+** — opcional: o projeto traz o *Maven Wrapper* (`mvnw` / `mvnw.cmd`),
  que baixa o Maven sozinho na primeira execução.

### Comandos

Na raiz do projeto:

```bash
# rodar os testes unitários
mvn clean test          # ou: ./mvnw clean test   (Windows: mvnw.cmd clean test)

# rodar o sistema (menu de console)
mvn spring-boot:run     # ou: ./mvnw spring-boot:run   (Windows: mvnw.cmd spring-boot:run)
```

> **Windows:** se os acentos aparecerem trocados no terminal, rode `chcp 65001` antes
> para usar UTF-8 no console.

### Banco de dados

- **H2 em arquivo**, criado automaticamente em `./data/territorio.mv.db` na primeira execução.
- As tabelas são criadas pelo Hibernate (`ddl-auto=update`), então **os dados continuam lá
  depois de fechar e abrir o programa**.
- Para começar do zero, basta apagar a pasta `data/`.
- A pasta `data/` e os `.csv` exportados estão no `.gitignore`.

### Roteiro rápido de demonstração

1. Opção **1** algumas vezes (≥ 5) → lê os 4 sensores e grava as medições.
   De vez em quando um sensor "falha" (5% de chance) e aparece um aviso — os outros continuam sendo lidos.
2. Opção **3** → roda a previsão de cada sensor e mostra os alertas emitidos.
   Com menos de 5 leituras aparece: *"Dados insuficientes — o sistema prefere não prever"*.
3. Opção **2** com valor `-3` no sensor `PLU-01` → a medição é recusada com mensagem clara.
4. Opção **5** → altera limiares e estratégia padrão. Código de acesso fictício: **`TPI-2026`**.
5. Opção **6** → relatório completo (montado pelo Builder).
6. Opção **7** → exporta o histórico de um sensor para `historico_<sensor>_<data>.csv`.
7. Feche (opção **0**), abra de novo e use a opção **4** → o histórico continua lá.

---

## 4. Arquitetura e decisões da equipe

### Pacotes (`br.com.fiap.tpi`)

| Pacote | Responsabilidade | Principais classes |
|---|---|---|
| raiz | Inicialização do Spring e menu de console | `SistemaMonitoramento` |
| `domain` | Modelo do território, sensores, previsões e entidades JPA | `ComponenteTerritorial`, `PontoMonitoramento`, `Sensor`, `SensorSimulado`, `SensorPluviometro`, `SensorNivelRio`, `SensorUmidadeSolo`, `TipoSensor`, `StatusComponente`, `DadosTerritorio`, `EstrategiaPrevisao`, `PrevisaoMediaMovel`, `PrevisaoComparacao`, `TipoPrevisao`, `ResultadoPrevisao`, `Medicao`, `Alerta`, `NivelAlerta`, `Relatorio` |
| `exception` | Exceções verificadas do domínio | `DadosInvalidosException`, `FonteIndisponivelException` |
| `repository` | Acesso ao banco com Spring Data JPA | `MedicaoRepository`, `AlertaRepository` |
| `service` | Regras de negócio, configuração e padrões criacionais | `ServicoMonitoramento`, `ConfiguracaoSistema`, `FabricaPrevisao`, `RelatorioBuilder`, `ExportadorCsv` |

O fluxo é sempre **menu → serviço → domínio/repositório**. O menu não acessa o banco
diretamente; ele só conversa com o `ServicoMonitoramento` e captura as exceções para mostrar
mensagens amigáveis.

### Conceitos de POO aplicados

- **Abstração e herança:** `ComponenteTerritorial` é abstrata, com o método abstrato
  `verificarStatus()`. `PontoMonitoramento` e os três sensores herdam dela.
  Os sensores compartilham a simulação em `SensorSimulado` (também abstrata), evitando código repetido.
- **Interface e polimorfismo:** `Sensor` tem três implementações. O menu e o serviço trabalham
  só com `Sensor` e com `EstrategiaPrevisao`; quem decide qual `lerValor()` / `prever()` roda é
  o polimorfismo, sem `instanceof`.
- **Sobrescrita com `super`:** `PrevisaoMediaMovel` e `PrevisaoComparacao` sobrescrevem
  `prever()` e chamam `super.prever(historico)`, que aplica a regra comum "menos de 5 leituras → não prevê".
- **Encapsulamento:** atributos privados com setters validados; `codigoAcesso` de `DadosTerritorio`
  não tem getter; `PontoMonitoramento` devolve a lista de sensores somente leitura.

### Por que Spring Data JPA + H2 em arquivo

- **JPA** elimina SQL escrito à mão: as consultas são métodos derivados
  (`findBySensorIdOrderByDataHoraAsc`, `findAllByOrderByDataHoraDesc`), então **não existe SQL
  concatenado com String** e não há risco de *SQL injection*.
- O **Spring gerencia conexões e transações** — não precisamos abrir/fechar `Connection` manualmente.
- **H2 em arquivo** não exige instalar servidor de banco: quem for corrigir só precisa do JDK.
  E, diferente do H2 em memória, **os dados sobrevivem ao reinício**, o que é requisito da GS.
- `spring.main.web-application-type=none`: o sistema é de console, não sobe servidor web.

### Por que esses limiares

| Tipo | Atenção | Alerta | Crítico | Justificativa |
|---|---|---|---|---|
| Chuva (mm/h) | 20 | 40 | 60 | Acima de ~20 mm/h a chuva já é considerada forte e a drenagem urbana começa a saturar; 40–60 mm/h costuma provocar alagamentos rápidos em pontos baixos. |
| Nível do rio (m) | 3,0 | 4,0 | 5,0 | Pensados para um córrego urbano com régua de 0 a 10 m e transbordamento perto dos 5 m: a equipe ganha margem para agir antes do limite. |
| Umidade do solo (%) | 70 | 85 | 95 | Solo próximo da saturação perde capacidade de absorver água e aumenta o escoamento superficial e o risco em taludes. |

São **valores de referência para a simulação**, não oficiais. Por isso podem ser ajustados
pelo menu (opção 5), com código de acesso e validação: o valor precisa estar dentro da faixa
física do sensor e manter a ordem **Atenção < Alerta < Crítico**.

### Exceções

- `DadosInvalidosException` — medição fora da faixa (ex.: chuva negativa), população negativa,
  limiar incoerente, código de acesso errado, opção de menu inexistente.
- `FonteIndisponivelException` — sensor que não respondeu na leitura.
- Ambas são **verificadas** (`extends Exception`), então o compilador obriga a tratá-las.
- São lançadas nos pontos de validação (construtores, setters, serviço) e **capturadas no menu**,
  sempre com mensagem explicando o problema concreto. Não existe `catch` vazio.

---

## 5. Padrões criacionais

### Factory — `FabricaPrevisao`

**Onde:** criação da estratégia de previsão (`MEDIA_MOVEL` ou `COMPARACAO`).

**Por quê:** sem a fábrica, todo lugar que precisasse de uma previsão teria um
`if (tipo == MEDIA_MOVEL) new PrevisaoMediaMovel(...) else ...`. A `FabricaPrevisao` guarda um
`Map<TipoPrevisao, Supplier<EstrategiaPrevisao>>` montado no construtor e o método `criar(tipo)`
só busca no mapa. O resto do sistema conhece apenas `EstrategiaPrevisao`.
Para adicionar uma nova estratégia basta **uma linha** no construtor da fábrica.

**Onde é chamada:** `ServicoMonitoramento.rodarPrevisao(...)`, usado pelas opções 3 e 6 do menu.

### Builder — `RelatorioBuilder`

**Onde:** montagem do relatório de "status completo" (opção 6).

**Por quê:** o `Relatorio` tem várias partes — dados do território, status dos sensores,
últimas previsões e alertas recentes — e algumas são opcionais (no início pode não haver
alertas). Um construtor com cinco ou mais parâmetros seria difícil de ler e fácil de errar a ordem.
Com o Builder cada parte entra com um nome claro e o `build()` valida o que é obrigatório:

```java
Relatorio relatorio = new RelatorioBuilder()
        .comTerritorio(territorio)
        .comStatusSensores(sensores)
        .comUltimasPrevisoes(previsoes)
        .comAlertasRecentes(recentes)
        .build();
```

### Por que não em outros lugares

- **Medicao / Alerta:** têm poucos campos, todos obrigatórios — um construtor (ou a fábrica
  estática `Medicao.criar(...)`, que valida a faixa) já resolve; um Builder seria só cerimônia.
- **Sensores:** são criados uma única vez, na montagem do território; não há escolha dinâmica
  de tipo que justifique outra fábrica.
- **Singleton:** a `ConfiguracaoSistema` é um bean comum do Spring. O container já garante uma
  instância compartilhada, então implementar o Singleton manualmente seria duplicar o que o
  Spring faz — e dificultaria os testes.

---

## 6. Testes unitários

25 testes em `src/test/java/br/com/fiap/tpi`, todos no padrão **AAA** (`// Arrange`, `// Act`,
`// Assert`), **sem banco e sem console** — os repositórios são substituídos por mocks do Mockito.

| Classe de teste | O que verifica |
|---|---|
| `MedicaoTest` (3) | chuva negativa e valor acima do máximo lançam `DadosInvalidosException` (`assertThrows`); medição válida mantém valor e tipo |
| `PrevisaoMediaMovelTest` (3) | [10, 20, 30, 40, 50] com janela 5 → 30,0; usa só as últimas N leituras; menos de 5 leituras → não confiável |
| `PrevisaoComparacaoTest` (3) | tendência de alta projeta valor maior que a média recente; tendência de queda nunca prevê chuva negativa; menos de 5 leituras → não confiável |
| `FabricaPrevisaoTest` (3) | `criar(MEDIA_MOVEL)` devolve `PrevisaoMediaMovel`; `criar(COMPARACAO)` devolve `PrevisaoComparacao`; tipo nulo é recusado |
| `ServicoMonitoramentoTest` (7) | com `@Mock` dos repositórios: previsão acima do CRÍTICO gera alerta CRÍTICO e chama `alertaRepository.save(...)` (`verify`); entre ALERTA e CRÍTICO gera nível ALERTA; abaixo de ATENÇÃO e previsão não confiável não gravam nada (`never()`); leitura manual válida é gravada e inválida não; previsão usa o histórico do repositório e a estratégia da fábrica |
| `DadosTerritorioTest` (4) | código de acesso errado e população negativa lançam `DadosInvalidosException`; código correto altera o limiar; ATENÇÃO maior que ALERTA é recusado |
| `ExportadorCsvTest` (2) | o CSV tem cabeçalho e uma linha por medição; histórico vazio é recusado |

O teste `contextLoads()` do Spring Initializr foi removido de propósito: ele subiria o contexto
completo e ficaria travado esperando o `Scanner` do menu.

---

## 7. Reflexão crítica

**Quando o sistema deve se calar?**
Com menos de **5 leituras** de um sensor, nenhuma estratégia faz previsão: o resultado volta
marcado como não confiável, o menu mostra *"Dados insuficientes — o sistema prefere não prever"*
e **nenhum alerta é emitido**. Uma média de duas leituras não diz nada sobre a próxima hora de
chuva, e um alerta falso tem custo real: se a comunidade recebe avisos que não se confirmam,
ela deixa de levar a sério o aviso que importa. Preferimos o silêncio honesto a uma previsão
inventada. Pelo mesmo motivo, um sensor que falha na leitura não gera medição "chutada": a
leitura é descartada e o usuário é avisado.

**O que os dados não representam?**
Os dados são **simulados** com valores aleatórios dentro de faixas realistas; não vêm de
sensores físicos e não seguem a dinâmica real de uma chuva (que tem início, pico e fim). Mesmo
com dados reais, **um sensor por ponto diz pouco**: um pluviômetro mede a chuva naquele lugar,
não a de toda a bacia, e o nível do córrego depende também do que choveu rio acima, da
impermeabilização do solo e de bueiros entupidos — nada disso está no modelo. As estratégias
(média móvel e comparação de períodos) são propositalmente simples; servem para indicar
tendência, não para substituir a previsão meteorológica oficial. Os limiares também são
referências da equipe, não números oficiais da defesa civil.

**Por que o alerta apoia a decisão em vez de decidir?**
Quem conhece o território sabe coisas que o sistema não sabe: que a escola já foi esvaziada,
que a equipe acabou de limpar o bueiro, que a água naquela rua escoa rápido. Por isso o alerta
**explica** o que viu — qual sensor, qual valor previsto, qual limite foi ultrapassado e com
qual estratégia — para que a pessoa responsável confira e decida se aciona a comunidade,
interdita a via ou abre o abrigo. O sistema não dispara sirene nem manda mensagem sozinho, e o
próprio menu lembra: *"Os alertas apoiam a decisão da equipe local — não substituem a avaliação
humana."* Errar é possível dos dois lados (alerta falso ou alerta perdido), e a responsabilidade
por agir precisa ficar com pessoas que podem avaliar o contexto e responder por isso.

---

## 8. Uso de IA

_(A equipe deve preencher com honestidade: em quais partes a IA foi usada — por exemplo, para
tirar dúvidas de conceitos, sugerir estrutura ou revisar textos — e como cada integrante revisou,
testou e entendeu o código antes de entregar.)_

---

## 9. Divisão de tarefas

| Integrante | Responsabilidade |
|---|---|
| _(preencher)_ | Domínio (`ComponenteTerritorial`, sensores, `DadosTerritorio`) + exceções customizadas |
| _(preencher)_ | Persistência (entidades JPA, repositórios, configuração do H2) + menu |
| _(preencher)_ | Previsões, alertas, padrões Factory/Builder e testes unitários |

---

## 10. Bônus implementado — Exportação para CSV (+0,5)

Opção **7** do menu. Exporta o histórico de medições de um sensor para
`historico_<sensorId>_<aaaaMMdd_HHmmss>.csv`, na pasta em que o programa foi iniciado:

```
id;sensor;regiao;tipo;valor;unidade;dataHora
1;Pluviômetro do Córrego;Zona Leste – Bacia do Córrego;CHUVA;30,9;mm/h;19/11/2026 02:27:43
```

- Separador `;` e vírgula decimal, para abrir direto no Excel/LibreOffice em português.
- Escrita com `try-with-resources` + `BufferedWriter` (o arquivo é fechado mesmo se der erro).
- `IOException` é capturada no menu com uma mensagem clara; sensor sem medições também é avisado.
