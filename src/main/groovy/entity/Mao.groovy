package entity
/**
 * Created by pedro on 19/07/17.
 */
class Mao {

    private boolean isSequencia
    private boolean isMesmoNaipe
    private int totalNoMaiorPar
    private int totalNoMenorPar
    private Categoria categoria
    private int totalGrupos
    private int cartaMaisAlta
    private List<Carta> cartas

    Mao(String stringCartas) {
        this.cartas = convertToListCartas(stringCartas)
        sortHand()
        verifyNaipe()
        verifySequencia()
        verifyGroups()
        determinarCartaMaisAlta()
        determineCategory()
    }

    Result compareWith(Mao opponent) {
        if (this.categoria.ordinal() > opponent.categoria.ordinal()) {
            return Result.WIN
        } else if (this.categoria.ordinal() < opponent.categoria.ordinal()) {
            return Result.LOSS
        } else {
            return desempate(opponent)
        }
    }

    private Result desempate(Mao opponent) {
        def myMap = getCartasAgrupadas(this.cartas)
        def opponentMap = getCartasAgrupadas(opponent.cartas)

        List<List<Carta>> myList = []
        List<List<Carta>> opponentList = []
        myMap.each { k, v -> myList << v }
        opponentMap.each { k, v -> opponentList << v }

        if (myList.get(0).size() > 1) {
            Result r = getDesempateResult(myList.get(0).get(0).valor.ordinal(), opponentList.get(0).get(0).valor.ordinal())
            if (r.is(Result.DRAW)) {
                if (myList.get(1).size() > 1) {
                    r = getDesempateResult(myList.get(1).get(0).valor.ordinal(), opponentList.get(1).get(0).valor.ordinal())
                    if (r.is(Result.DRAW)) {
                        return desempateByKicker(myList, opponentList)
                    } else {
                        return r
                    }
                } else {
                    r = desempateByKicker(myList, opponentList)
                }
            }
            return r
        } else {
            return desempateByKicker(myList, opponentList)
        }
    }

    private Map getCartasAgrupadas(List<Carta> paramCartas) {
        return paramCartas.groupBy { it.valor.ordinal() }.sort({ primeira, segunda ->
            if (segunda.value.size() > primeira.value.size()) {return 1}
            if (segunda.value.size() == primeira.value.size()) {return -1}
            if (segunda.value.get(0).valor.ordinal() > primeira.value.get(0).valor.ordinal()) {return 1}
            return -1
        })
    }

    private void determineCategory() {
        if (isSequencia) {
            if (isMesmoNaipe) {
                if (cartaMaisAlta == 14) {
                    categoria = Categoria.ROYAL_FLUSH
                } else {
                    categoria = Categoria.STRAIGHT_FLUSH
                }
            } else {
                categoria = Categoria.SEQUENCIA
            }
        } else {
            if (isMesmoNaipe) {
                categoria = Categoria.FLUSH
            } else {
                if (totalNoMaiorPar == 4) {
                    categoria = Categoria.QUADRA
                } else {
                    if (totalNoMaiorPar == 3) {
                        if (totalGrupos == 2) {
                            categoria = Categoria.FULL_HOUSE
                        } else {
                            categoria = Categoria.TRINCA
                        }
                    } else {
                        if (totalGrupos == 2) {
                            categoria = Categoria.DOIS_PARES
                        } else {
                            if (totalGrupos == 1) {
                                categoria = Categoria.UM_PAR
                            } else {
                                categoria = Categoria.CARTA_ALTA
                            }
                        }
                    }
                }
            }
        }
    }

    private void verifyNaipe() {
        if (cartas.groupBy { it.naipe }.size() == 1) {
            isMesmoNaipe = true
        } else {
            isMesmoNaipe = false
        }
    }

    private void verifySequencia() {
        int numCartaAtual = (cartas.get(0).valor.ordinal() - 1)
        isSequencia = true

        cartas.each {
            if (it.valor.ordinal() == (numCartaAtual + 1)) {
                numCartaAtual = it.valor.ordinal()
            } else {
                isSequencia = false
            }
        }
    }

    private void verifyGroups() {
        def map = getCartasAgrupadas(this.cartas)
        totalNoMaiorPar = 0
        totalNoMenorPar = 0

        int somaGrupos = 0
        int contador = 1
        map.each {carta ->
            if (contador == 1 && carta.value.size() > 1) {
                totalNoMaiorPar = carta.value.size()
            } else if (contador == 2 && carta.value.size() > 1) {
                totalNoMenorPar = carta.value.size()
            }
            if (carta.value.size() > 1) {
                somaGrupos++
            }
            contador++
        }
        totalGrupos = somaGrupos
    }

    private void determinarCartaMaisAlta() {
        cartaMaisAlta = 0
        cartas.each {carta ->
            if (carta.valor.ordinal() > cartaMaisAlta) {
                cartaMaisAlta = carta.valor.ordinal()
            }
        }
    }

    private void sortHand() {
        cartas.sort({ it.valor.ordinal() })
    }

    private List<Carta> convertToListCartas(String paramCartas) {
        String[] arrayCartas = paramCartas.split(' ')
        List<Carta> listCartas = []
        arrayCartas.each { numCarta ->
            listCartas << new Carta(
                    valor: discoverValorCarta(numCarta.substring(0, 1)),
                    naipe: discoverNaipeCarta(numCarta.substring(1)))
        }
        return listCartas
    }

    private Valor discoverValorCarta(String letra) {
        switch (letra) {
            case ['2']:
                return Valor.DOIS
            case ['3']:
                return Valor.TRES
            case ['4']:
                return Valor.QUATRO
            case ['5']:
                return Valor.CINCO
            case ['6']:
                return Valor.SEIS
            case ['7']:
                return Valor.SETE
            case ['8']:
                return Valor.OITO
            case ['9']:
                return Valor.NOVE
            case 'T':
                return Valor.DEZ
            case 'J':
                return Valor.VALETE
            case 'Q':
                return Valor.DAMA
            case 'K':
                return Valor.REI
            case 'A':
                return Valor.AIS
            default:
                throw new Exception("Valor inválido")
        }
    }

    private Naipe discoverNaipeCarta(String naipe) {
        naipe = naipe.toUpperCase()
        switch (naipe) {
            case ['S']:
                return Naipe.ESPADA
            case ['H']:
                return Naipe.COPAS
            case ['D']:
                return Naipe.OURO
            case ['C']:
                return Naipe.PAUS
            default:
                throw new Exception("Naipe inválido")
        }
    }

    private Result getDesempateResult(int minhaCarta, int oponenteCarta) {
        if (minhaCarta > oponenteCarta) {
            return Result.WIN
        } else if (minhaCarta < oponenteCarta) {
            return Result.LOSS
        } else {
            return Result.DRAW
        }
    }

    private Result desempateByKicker(List<List<Carta>> minhaMao, List<List<Carta>> maoOponente) {
        Result result = null
        for (int i = 0; i < minhaMao.size(); i++) {
            if (minhaMao.get(i).size() < 2) {
                if (minhaMao.get(i).get(0).valor.ordinal() > maoOponente.get(i).get(0).valor.ordinal()) {
                    result = Result.WIN
                } else if (minhaMao.get(i).get(0).valor.ordinal() < maoOponente.get(i).get(0).valor.ordinal()) {
                    result = Result.LOSS
                }
            }

        }
        if (result == null) {
            result = Result.DRAW
        }
        return result
    }
}


