/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.text

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

object StdoutRouterStream : OutputStream() {
	val defaultRoute = System.out

	init {
		System.setOut(StdoutRouter)
	}

	private val routePerThread = ThreadLocal<OutputStream>()

	override fun write(b: Int) {
		val os = routePerThread.get()
		if (os != null) {
			os.write(b)
		} else {
			defaultRoute.write(b)
		}
	}

	override fun write(b: ByteArray?, off: Int, len: Int) {
		val os = routePerThread.get()
		if (os != null) {
			os.write(b, off, len)
		} else {
			defaultRoute.write(b, off, len)
		}
	}

	fun <TOutputStream : OutputStream> routeTemporally(out:TOutputStream, callback: () -> Unit): TOutputStream {
		routePerThread.set(out)
		try {
			callback()
		} finally {
			routePerThread.set(null)
		}
		return out
	}

	fun captureThreadSafe(callback: () -> Unit) = routeTemporally(ByteArrayOutputStream(), callback)
}

object StdoutRouter : PrintStream(StdoutRouterStream) {
	val os = this.out
}

fun captureStdout(callback: () -> Unit): String {
	return StdoutRouterStream.captureThreadSafe(callback).toString()
}