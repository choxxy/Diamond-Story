package gameobjects

import com.badlogic.gdx.math.Vector2
import enums.EffectType
import enums.JewelType
import enums.MatchType
import utils.TexturesLoader
import java.util.*

// TODO: implement intArrayOf(0, 1, 1, 1) etc., also consider refactoring
class GameGrid(private val gridType : Array<IntArray>) {

    var cells = Array(gridType.count(), {_ -> Array(gridType[0].count()
            , {_ -> Cell(false, Jewel(JewelType.from(Random().nextInt(5)),
            EffectType.NONE),TexturesLoader.instance.tileBlank)})})

    init {
        for (i in gridType.indices) {
            for (j in gridType[i].indices) {
                cells[i][j].isPlaying = (gridType[i][j] == 1)

                // Making check no occurrences of 3 gems are presented
                if (i > 1) {
                    if (cells[i - 1][j].isPlaying)
                        if (cells[i - 2][j].isPlaying)
                            if (cells[i - 1][j].jewel.jewelType == cells[i][j].jewel.jewelType) {
                                while (cells[i - 2][j].jewel.jewelType == cells[i][j].jewel.jewelType)
                                    cells[i][j].jewel = Jewel(JewelType.from(Random().nextInt(5)), EffectType.valueOf("NONE"))
                            }
                }
                if (j > 1) {
                    if (cells[i][j - 1].isPlaying)
                        if (cells[i][j - 2].isPlaying)
                            if (cells[i][j - 1].jewel.jewelType == cells[i][j].jewel.jewelType) {
                                while (cells[i][j - 2].jewel.jewelType == cells[i][j].jewel.jewelType)
                                    cells[i][j].jewel = Jewel(JewelType.from(Random().nextInt(5)), EffectType.valueOf("NONE"))
                            }
                }

                val borders = getBorders(i, j, gridType)
                if (borders.contentEquals(intArrayOf(0, 0, 0, 1))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileTop
                }
                if (borders.contentEquals(intArrayOf(0, 1, 0, 0))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileRight
                }
                if (borders.contentEquals(intArrayOf(1, 0, 0, 0))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileDown
                }
                if (borders.contentEquals(intArrayOf(0, 0, 1, 0))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileLeft
                }
                if (borders.contentEquals(intArrayOf(1, 1, 0, 0))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileCornerRightDown
                }
                if (borders.contentEquals(intArrayOf(1, 0, 1, 0))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileCornerLeftDown
                }
                if (borders.contentEquals(intArrayOf(0, 0, 1, 1))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileCornerLeftTop
                }
                if (borders.contentEquals(intArrayOf(0, 1, 0, 1))) {
                    cells[i][j].tileTexture = TexturesLoader.instance.tileCornerRightTop
                }
            }
        }
    }

    // 1 if tile needs border, DOWN, RIGHT, LEFT, TOP
    private fun getBorders(i : Int, j : Int, cells : Array<IntArray>) : IntArray {
        val borders = intArrayOf(0, 0, 0, 0)
        if (i != 0) {
            if (cells[i - 1][j] == 0) {
                borders[2] = 1
            }
        } else {
            borders[2] = 1
        }
        if (j != 0) {
            if (cells[i][j - 1] == 0) {
                borders[0] = 1
            }
        } else {
            borders[0] = 1
        }
        if (i < cells.count() - 1) {
            if (cells[i + 1][j] == 0) {
                borders[1] = 1
            }
        } else {
            borders[1] = 1
        }
        if (j < cells[0].count() - 1) {
            if (cells[i][j + 1] == 0) {
                borders[3] = 1
            }
        } else {
            borders[3] = 1
        }
        return borders
    }

    fun inRange(x : Int, y : Int) : Boolean {
        return x >= 0 && y >= 0 && x < gridType.count() && y < gridType[0].count()
    }

    fun isAdjacent(x1 : Int, y1 : Int, x2 : Int, y2 : Int) : Boolean {
        if (((x1 - x2) <= 1 && (x1 - x2) >= -1)  &&
                ((y1 - y2) <= 1 && (y1 - y2) >= -1)) {
            if (!isDiagonalAdjacent(x1,y1,x2,y2)) {
                return true
            }
        }
        return false
    }

    private fun isDiagonalAdjacent(x1 : Int, y1 : Int, x2 : Int, y2 : Int) : Boolean {
        if ((x1 - x2) == (y1 - y2))
            return true
        if ((x1 + y1) == (x2 + y2))
            return true
        return false
    }

    fun swapCells(x1 : Int, y1 : Int, x2 : Int, y2 : Int) {
        val tmpCell = cells[x1][y1].jewel
        cells[x1][y1].jewel = cells[x2][y2].jewel
        cells[x2][y2].jewel = tmpCell
    }

    fun createsMatch(x : Int, y : Int, jewelType: JewelType) : Match {
        val matchHorizontal = getHorizontalMatch(x,y,jewelType)
        val matchVertical = getVerticalMatch(x,y,jewelType)
        val resultingMatch = Match(MutableList(1,{ _ -> Vector2(x.toFloat(),y.toFloat()) }),MatchType.NO_MATCH)
        if (matchHorizontal.gemsInMatch.count() > 1) {
            if (matchVertical.gemsInMatch.count() > 1) {
                matchHorizontal.mergeIn(matchVertical)
                resultingMatch.mergeIn(matchHorizontal)
                resultingMatch.matchType = MatchType.MATCH_CROSS
                return resultingMatch
            } else {
                resultingMatch.mergeIn(matchHorizontal)
            }
        } else {
            if (matchVertical.gemsInMatch.count() > 1)
                resultingMatch.mergeIn(matchVertical)
        }
        var size = resultingMatch.gemsInMatch.count()
        if (size > 5) size = 5
        if (size < 3) size = 0
        resultingMatch.matchType = MatchType.from(size)
        return resultingMatch
    }

    private fun getHorizontalMatch(x : Int, y : Int, jewelType: JewelType) : Match {
        var counter = 1
        val match = Match(mutableListOf(), MatchType.NO_MATCH)
        if (jewelType != JewelType.NO_JEWEL) {
            while ((x - counter) >= 0) {
                if (cells[x - counter][y].jewel.jewelType == jewelType) {
                    match.gemsInMatch.add(Vector2((x - counter).toFloat(), y.toFloat()))
                } else break
                counter++
            }
            counter = 1
            while ((x + counter) < cells.count()) {
                if (cells[x + counter][y].jewel.jewelType == jewelType) {
                    match.gemsInMatch.add(Vector2((x + counter).toFloat(), y.toFloat()))
                } else break
                counter++
            }
        }
        return match
    }

    private fun getVerticalMatch(x : Int, y : Int, jewelType: JewelType) : Match {
        var counter = 1
        val match = Match(mutableListOf(), MatchType.NO_MATCH)
        if (jewelType != JewelType.NO_JEWEL) {
            while ((y - counter) >= 0) {
                if (cells[x][y - counter].jewel.jewelType == jewelType) {
                    match.gemsInMatch.add(Vector2(x.toFloat(), (y - counter).toFloat()))
                } else break
                counter++
            }
            counter = 1
            while ((y + counter) < cells[0].count()) {
                if (cells[x][y + counter].jewel.jewelType == jewelType) {
                    match.gemsInMatch.add(Vector2(x.toFloat(), (y + counter).toFloat()))
                } else break
                counter++
            }
        }
        return match
    }

    fun removeMatch(match : Match) {
        for (gem in match.gemsInMatch) {
            cells[gem.x.toInt()][gem.y.toInt()].jewel.jewelType = JewelType.NO_JEWEL
        }
    }

}
