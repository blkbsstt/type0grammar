object Thue {
	import util.Random

	val defaultSep = " -> "

	case class Rule(lhs: String, rhs: String, sep: String) {
		override def toString = lhs + sep + rhs
	}

	def main(args: Array[String]): Unit = {
		var opts = options(args.toList)
		opts = opts.updated("sep", opts.get("sep").getOrElse(defaultSep))

		if (!(opts contains "file")) return println(usage)

		val (state, rules) = parse(opts("file"), opts("sep"))
		if (opts contains "debug") println(rules.mkString("\n"))
		if (!((opts contains "batch") || (opts contains "times")))
			opts += ("batch" -> "1")

		val strings = batch(state, rules, opts)
		if (!(opts contains "unsorted")) println(strings.toList.sorted.mkString("\n"))
	}

	val usage = """scala thue.scala [-b num] [-t num] [-s sep] [-d] [-u] program_file"""

	def options(args: List[String], opts: Map[String, String] = Map.empty): Map[String, String] =
		args match {
			case "-b" :: i :: rest ⇒ options(rest, opts + ("batch" -> i))
			case "-t" :: i :: rest ⇒ options(rest, opts + ("times" -> i))
			case "-s" :: sep :: rest => options(rest, opts + ("sep" -> sep))
			case "-d" :: rest ⇒ options(rest, opts + ("debug" -> "true"))
			case "-u" :: rest ⇒ options(rest, opts + ("unsorted" -> "true"))
			case file :: Nil ⇒ opts + ("file" -> file)
			case _ ⇒ opts
		}

	def parse(filename: String, sep: String) = {
		val input = io.Source.fromFile(filename).getLines
		val state = input.next

		val lhs = """(\S+)"""
		val rhs = """(\S*)"""
		val InputRegEx = (lhs + sep + rhs).r
		val Whitespace = """\s+""".r

		val rules = input.flatMap {
			case InputRegEx(lhs, rhs) ⇒ Some(Rule(lhs, rhs, sep))
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
			else {
				val s = execute(state, rules, opts)
				if ((opts contains "unsorted") && !strings(s)) println(s)
				loop(strings + s, t + 1)
			}
		loop(Set.empty[String], 0)
	}
}
