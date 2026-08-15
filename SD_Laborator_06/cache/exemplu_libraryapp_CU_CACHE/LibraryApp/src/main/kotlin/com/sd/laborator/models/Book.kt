package com.sd.laborator.models

data class Book(var id: Int, private var data: Content) {

    var name: String?
        get() {
            return data.name
        }
        set(value) {
            data.name = value
        }

    var author: String?
        get() {
            return data.author
        }
        set(value) {
            data.author = value
        }

    var publisher: String?
        get() {
            return data.publisher
        }
        set(value) {
            data.publisher = value
        }

    var text: String?
        get() {
            return data.text
        }
        set(value) {
            data.text = value
        }

}