welcome = { ->
    def hostName = "r1";
    return """\
Welcome to $hostName
It is ${new Date(System.currentTimeMillis() + 1000l * 60 * 60 * 24 * 365 * 100)} now
""";
}

prompt = { ->
    return "% ";
}
