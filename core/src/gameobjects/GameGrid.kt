package gameobjects

import enums.EffectType
import enums.JewelType
import utils.TexturesLoader
import java.util.*

// TODO: implement intArrayOf(0, 1, 1, 1) etc.
class GameGrid(private val gridType : Array<IntArray>) {

    var cells = Array(gridType[0].count(), {_ -> Array(gridType[0].count()
            , {_ -> Cell(false, Jewel(JewelType.from(Random().nextInt(5)),
            EffectType.NONE),TexturesLoader.instance.tileBlank)})})

    init {
        for (i in gridType.indices) {
            for (j in gridType[i].indices) {
                cells[i][j].isPlaying = (gridType[i][j] == 1)
                val borders = getBorders(i,j,cells[0].count() - 1, gridType)
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
    fun getBorders(i : Int, j : Int, size : Int, cells : Array<IntArray>) : IntArray {
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
        if (i < size) {
            if (cells[i + 1][j] == 0) {
                borders[1] = 1
            }
        } else {
            borders[1] = 1
        }
        if (j < size) {
            if (cells[i][j + 1] == 0) {
                borders[3] = 1
            }
        } else {
            borders[3] = 1
        }
        return borders
    }

}