<h1 align="center">🐔 RINHA DE BACKEND 2023 Q3™ em Kotlin 🐔</h1>
 
Uma implementação feita em [Kotlin](https://kotlinlang.org), pois Kotlin is my beloved. <img src="https://cdn.discordapp.com/emojis/841285914159611914.gif" height="24" />

É uma implementação bem básica, feita em menos de 3 horas. Algumas coisas estão faltando, mas tudo funciona. *insira estampa de "funciona na minha máquina" aqui*.

O sistema foi implementado usando Ktor (Web Server), Exposed (para comunicar com o banco de dados usando uma DSL), Nginx (Load Balancer), PostgreSQL (Banco de Dados) e, é claro, Docker (Gerenciador de Containers). Sim, sem cache, pois a gente carrega os dados na marra, direto do banco de dados. Aqui é natty e não fake natty desgraça. 💪

Espero que gostem! Ah, e se você quiser ver o código-fonte, ele [está aqui](https://github.com/MrPowerGamerBR/RinhaDeBackend2023Q3Kotlin)!

(A única coisa que eu "propositalmente" não implementei, foi a parte de retornar Bad Request caso o JSON esteja sinteticamente incorreto, pois infelizmente tentar verificar a diferença entre "o nome é null então isso deve ser um Unprocessable Entity" e verificar "o nome é um número então isso deve ser um Bad Request" é difícil pelo kotlinx.serialization, já que ele acaba retornando a mesma exception independentemente de qual foi o problema na hora de deserializar o JSON)

Enquanto, por algum motivo, a minha entry não foi pontuada devido a algum problema que teve, eu realizei os testes:

A versão original que foi enviada para o evento obteve 6526 pessoas inseridas, nada mal para uma implementação feita em uma tarde.

<img width="1127" alt="WindowsTerminal_W7gbzoDumI" src="https://github.com/MrPowerGamerBR/RinhaDeBackend2023Q3Kotlin/assets/9496359/65322ef6-8975-4dbd-bf24-80fa5361b2b7">

Após eu descobrir que [a minha participação não foi testada com sucesso](https://twitter.com/MrPowerGamerBR/status/1695566587040817181), eu decidi tentar ir além... [This, is to go, even further beyond!](https://youtu.be/8TGalu36BHA)

(Eu não sei porque não funcionou, mas eu percebi que, as vezes, tem chance do container do nginx iniciar antes da API da rinha e aí o nginx sai com um erro pois o hostname da API não existe)

<img width="1114" alt="WindowsTerminal_ywFy7noEcg" src="https://github.com/MrPowerGamerBR/RinhaDeBackend2023Q3Kotlin/assets/9496359/f5daecc0-0254-4a5c-a97e-08bb0fe3d1a4">

**📝 As mudanças que foram feitas:**
- Otimização no insert de pessoas, evitando o máximo roundtrips ao PostgreSQL
- Agora só tem um servidor de API ao invés de dois, assim evitando o overhead de memória que custa duas JVMs
- A API e a Database agora foram balanceados de forma melhor, assim evitando ao máximo que o throughput diminua devido a bottleneck em alguma das duas partes
- PostgreSQL agora tem a extensão de `pg_trgm` instalada e com indexes GiST criados no nome/apelido das pessoas e no nome dos stacks

Com as mudanças e as otimizações que eu fiz, a implementação foi de míseras 6526 pessoas para 19434 pessoas, nada mal para uma implementação feita com muita preguiça, e é a segunda melhor implementação na JVM!

## 🔗 Redes Sociais
* **GitHub:** [`@MrPowerGamerBR`](https://github.com/MrPowerGamerBR)
* **Twitter/X:** [`@MrPowerGamerBR`](https://twitter.com/MrPowerGamerBR)
* **Website:** [`mrpowergamerbr.com`](https://mrpowergamerbr.com/)
