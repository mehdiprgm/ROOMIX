package org.zendev.roomix.request

import org.zendev.roomix.request.basics.BaseRequest

class StringRequest: BaseRequest() {
    var min = "10"
    var max = "20"
    var includeSymbols = false
}