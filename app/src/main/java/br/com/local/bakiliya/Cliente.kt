package br.com.local.bakiliya

data class Cliente(val Nome: String ?= null, val Codigo: String ?= null, val Email: String ?= null, val Contato: String ?= null, val CEP: String ?= null, val NRua: String ?= null, var Tipo: String ?= null)
