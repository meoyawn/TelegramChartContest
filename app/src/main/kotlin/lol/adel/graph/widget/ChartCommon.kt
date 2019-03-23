package lol.adel.graph.widget

import help.Idx
import help.X
import help.Y

fun fill(buff: FloatArray, iBuf: Idx, x: X, y: Y): Idx {
    // end of prev line
    buff[iBuf + 0] = x
    buff[iBuf + 1] = y

    // start of next line
    buff[iBuf + 2] = x
    buff[iBuf + 3] = y

    return iBuf + 4
}
