object Thue {
	import util.Random

	val sep = " -> "

	case class Rule(lhs: String, rhs: String) {
		override def toString = lhs + sep + rhs
	}

	def main(args: Array[String]): Unit = {
		var opts = options(args.toList)

		if (!(opts contains "file")) return println(usage)

		val (state, rules) = parse(opts("file"))

		if (opts contains "debug") println(rules.mkString("\n"))

		if (!((opts contains "batch") || (opts contains "times")))
			opts += ("batch" -> "1")

		println(batch(state, rules, opts).toList.sorted.mkString("\n"))
	}

	val usage = """thue [-d] [-[Bb] num] [-t num] <program_file>"""

	def options(args: List[String], opts: Map[String, String] = Map.empty): Map[String, String] =
		args match {
			case "-b" :: i :: rest ⇒ options(rest, opts + ("batch" -> i))
			case "-t" :: i :: rest ⇒ options(rest, opts + ("times" -> i))
			case "-d" :: rest ⇒ options(rest, opts + ("debug" -> "true"))
			case file :: Nil ⇒ opts + ("file" -> file)
			case _ ⇒ opts
		}

	def parse(filename: String) = {
		val input = io.Source.fromFile(filename).getLines
		val state = input.next

		val lhs = """(\S+)"""
		val rhs = """(\S*)"""
		val InputRegEx = (lhs + sep + rhs).r
		val Whitespace = """\s+""".r

		val rules = input.flatMap {
			case InputRegEx(lhs, rhs) ⇒ Some(Rule(lhs, rhs))
			case Whitespace ⇒ None
			case line ⇒ println("Malformed production: " + line); None
		} toList

		(state, rules)
	}

	def execute(state: String, rules: List[Rule], opts: Map[String, String]): String = {
		if (opts contains "debug") println("step: " + state)
		val matches = rules.flatMap(r ⇒ r.lhs.r.findAllIn(state).matchData.map((_, r.rhs))).toArray

		if (matches isEmpty) return state

		val (matchData, rhs) = matches(Random.nextInt(matches.size))
		val newState = matchData.before + rhs + matchData.after
		execute(newState, rules, opts)
	}

	def batch(state: String, rules: List[Rule], opts: Map[String, String]) = {
		def loop(strings: Set[String], t: Int): Set[String] =
			if (((opts contains "batch") && (strings.size >= opts("batch").toInt))
				|| ((opts contains "times") && (t >= opts("times").toInt))) strings
			else loop(strings + execute(state, rules, opts), t + 1)
		loop(Set.empty[String], 0)
	}
}
