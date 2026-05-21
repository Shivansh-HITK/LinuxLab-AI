package com.example.terminal

import com.example.filesystem.VirtualFileSystem
import com.example.filesystem.VFile
import com.example.filesystem.VDirectory
import java.util.Locale

data class CommandResult(
    val output: String,
    val isSuccess: Boolean = true,
    val isCleared: Boolean = false,
    val errorTip: String? = null
)

object CommandExecutor {

    suspend fun execute(cmdLine: String, fs: VirtualFileSystem, onHelpRequested: suspend (String) -> String): CommandResult {
        val trimmed = cmdLine.trim()
        if (trimmed.isEmpty()) {
            return CommandResult("")
        }

        // Check for redirection operators > or >>
        val redirectionPipe = checkForRedirection(trimmed)
        val workingCmd = if (redirectionPipe != null) trimmed.substring(0, redirectionPipe.operatorIndex).trim() else trimmed
        
        // Split with spaces, preserving double quoted strings
        val parts = parseArguments(workingCmd)
        if (parts.isEmpty()) {
            return CommandResult("")
        }

        val cmdName = parts[0].lowercase(Locale.ROOT)
        val args = parts.drop(1)

        // Handle specific auto-correct queries
        val typoResult = checkForCommonTypos(cmdName)
        if (typoResult != null) {
            return typoResult
        }

        // Intercept Gemini HELP triggers
        if (cmdName == "help" || cmdName == "explain" || cmdName == "tutor") {
            if (args.isEmpty()) {
                val helpMsg = "📋 **LinuxLab AI Terminal Cheat-Sheet**\n" +
                        "───────────────────────────────────────────\n" +
                        "• **File Listing**: `ls`, `ls -la` (shows permissions & detail)\n" +
                        "• **Navigation**: `cd <folder>`, `cd ..` (climb parent), `pwd` (print path)\n" +
                        "• **Creation**: `mkdir <name>` (folder), `touch <file>` (empty file)\n" +
                        "• **Deletion**: `rm <file>`, `rm -rf <folder>`\n" +
                        "• **Relocation**: `mv <source> <dest>`, `cp <source> <dest>`\n" +
                        "• **Viewing**: `cat <file>`, `head -5 <file>` (first lines), `tail -5 <file>`\n" +
                        "• **Filtering & Editing**: `grep <pattern> <file>`, `echo <text>`\n" +
                        "• **Permissions**: `chmod +x <file>`, `chmod 755 <file>`\n" +
                        "───────────────────────────────────────────\n" +
                        "💡 **AI Assistant Power-Up**: Run `help <command>` (e.g. `help grep`) to activate Gemini's detailed explanation tutorials!"
                return CommandResult(helpMsg)
            } else {
                val targetHelpCmd = args[0]
                val explanation = onHelpRequested(targetHelpCmd)
                return CommandResult(explanation)
            }
        }

        if (cmdName == "clear") {
            return CommandResult("", isSuccess = true, isCleared = true)
        }

        return try {
            val responseText = when (cmdName) {
                "pwd" -> fs.pwd()
                "ls" -> {
                    val pathArg = args.filter { !it.startsWith("-") }.firstOrNull() ?: ""
                    val files = fs.ls(pathArg)
                    if (files.isEmpty()) {
                        "(Directory is empty)"
                    } else {
                        files.joinToString("\n") { (name, isDir) ->
                            if (isDir) "📁  $name/" else "📄  $name"
                        }
                    }
                }
                "cd" -> {
                    if (args.isEmpty()) {
                        fs.cd("~")
                    } else {
                        fs.cd(args[0])
                    }
                }
                "mkdir" -> {
                    if (args.isEmpty()) throw Exception("mkdir: missing operand")
                    fs.mkdir(args[0])
                    "Created directory: '${args[0]}'"
                }
                "touch" -> {
                    if (args.isEmpty()) throw Exception("touch: missing file operand")
                    fs.touch(args[0])
                    "Created/updated file: '${args[1].takeIf { false } ?: args[0]}'"
                }
                "rm" -> {
                    val recursive = args.contains("-rf") || args.contains("-r")
                    val targets = args.filter { it != "-rf" && it != "-r" }
                    if (targets.isEmpty()) throw Exception("rm: missing operand")
                    fs.rm(targets[0], recursive)
                    "Removed: '${targets[0]}'"
                }
                "mv" -> {
                    if (args.size < 2) throw Exception("mv: missing destination file operand")
                    fs.mv(args[0], args[1])
                    "Moved '${args[0]}' to '${args[1]}'"
                }
                "cp" -> {
                    if (args.size < 2) throw Exception("cp: missing destination file operand")
                    fs.cp(args[0], args[1])
                    "Copied '${args[0]}' to '${args[1]}'"
                }
                "cat" -> {
                    if (args.isEmpty()) throw Exception("cat: missing file operand")
                    fs.cat(args[0])
                }
                "echo" -> {
                    val echoText = args.joinToString(" ")
                    if (redirectionPipe != null) {
                        fs.echo(echoText, redirectionPipe.targetFile, redirectionPipe.isAppend)
                        "Redirected output to '${redirectionPipe.targetFile}'"
                    } else {
                        fs.echo(echoText)
                    }
                }
                "grep" -> {
                    // Usage e.g.: grep "pattern" file.txt
                    if (args.size < 2) throw Exception("grep: missing query or file parameter")
                    val query = args[0].removeSurrounding("\"").removeSurrounding("'")
                    val file = args[1]
                    fs.grep(query, file)
                }
                "chmod" -> {
                    if (args.size < 2) throw Exception("chmod: missing mode or file parameter")
                    fs.chmod(args[0], args[1])
                    "Changed file permissions configuration for '${args[1]}'."
                }
                "find" -> {
                    if (args.isEmpty()) throw Exception("find: missing match name operand")
                    fs.find(args[0])
                }
                "head" -> {
                    if (args.isEmpty()) throw Exception("head: missing file operand")
                    val count = args.find { it.startsWith("-") }?.removePrefix("-")?.toIntOrNull() ?: 10
                    val file = args.last()
                    fs.head(file, count)
                }
                "tail" -> {
                    if (args.isEmpty()) throw Exception("tail: missing file operand")
                    val count = args.find { it.startsWith("-") }?.removePrefix("-")?.toIntOrNull() ?: 10
                    val file = args.last()
                    fs.tail(file, count)
                }
                else -> {
                    throw IllegalStateException("Command not found: $cmdName")
                }
            }
            
            // Success response
            CommandResult(output = responseText, isSuccess = true)
        } catch (e: Exception) {
            val errString = e.message ?: "An execution error occurred."
            val tipText = getSmartTip(cmdName, args, fs, errString)
            CommandResult(
                output = "❌ Error: $errString",
                isSuccess = false,
                errorTip = tipText
            )
        }
    }

    private data class RedirectionInfo(
        val operatorIndex: Int,
        val targetFile: String,
        val isAppend: Boolean
    )

    private fun checkForRedirection(cmdLine: String): RedirectionInfo? {
        val appendIdx = cmdLine.indexOf(">>")
        if (appendIdx != -1) {
            val fileSeg = cmdLine.substring(appendIdx + 2).trim()
            return RedirectionInfo(appendIdx, fileSeg, isAppend = true)
        }
        val outIdx = cmdLine.indexOf(">")
        if (outIdx != -1) {
            val fileSeg = cmdLine.substring(outIdx + 1).trim()
            return RedirectionInfo(outIdx, fileSeg, isAppend = false)
        }
        return null
    }

    private fun parseArguments(cmdLine: String): List<String> {
        val result = mutableListOf<String>()
        var currentToken = java.lang.StringBuilder()
        var insideQuotes = false
        var quoteChar = ' '

        var i = 0
        while (i < cmdLine.length) {
            val c = cmdLine[i]
            if (insideQuotes) {
                if (c == quoteChar) {
                    insideQuotes = false
                } else {
                    currentToken.append(c)
                }
            } else {
                if (c == '"' || c == '\'') {
                    insideQuotes = true
                    quoteChar = c
                } else if (c.isWhitespace()) {
                    if (currentToken.isNotEmpty()) {
                        result.add(currentToken.toString())
                        currentToken = java.lang.StringBuilder()
                    }
                } else {
                    currentToken.append(c)
                }
            }
            i++
        }
        if (currentToken.isNotEmpty()) {
            result.add(currentToken.toString())
        }
        return result
    }

    private fun checkForCommonTypos(cmd: String): CommandResult? {
        return when (cmd) {
            "sl", "lss", "ks" -> CommandResult(
                output = "❌ command not found: $cmd\n💡 Did you mean 'ls'?",
                isSuccess = false,
                errorTip = "Hint: 'ls' lists the files and directories inside your current folder directory context. Let's practice with 'ls'!"
            )
            "dc", "cdd" -> CommandResult(
                output = "❌ command not found: $cmd\n💡 Did you mean 'cd'?",
                isSuccess = false,
                errorTip = "Hint: 'cd <folder>' moves your directory focus. Run 'cd /' or 'cd projects' to check it out!"
            )
            "pw", "pwdd" -> CommandResult(
                output = "❌ command not found: $cmd\n💡 Did you mean 'pwd'?",
                isSuccess = false,
                errorTip = "Hint: 'pwd' stands for 'Print Working Directory'. It outputs your absolute coordinates path."
            )
            "mkdirr", "mkkdir" -> CommandResult(
                output = "❌ command not found: $cmd\n💡 Did you mean 'mkdir'?",
                isSuccess = false,
                errorTip = "Hint: use 'mkdir <folder_name>' to generate folders. Standard spelling is clean and compact."
            )
            "claer", "cls" -> CommandResult(
                output = "❌ command not found: $cmd\n💡 Did you mean 'clear'?",
                isSuccess = false,
                errorTip = "Hint: 'clear' wipes the terminal board clean of old logs so you can concentrate."
            )
            else -> null
        }
    }

    private fun getSmartTip(cmd: String, args: List<String>, fs: VirtualFileSystem, errorText: String): String {
        val lowerErr = errorText.lowercase()
        return when {
            lowerErr.contains("not found") || lowerErr.contains("command not found") -> {
                "This command isn't configured in our sandbox simulation. Make sure you don't have typos!\n" +
                "💡 Try running: `help` to see the lists of compatible commands."
            }
            lowerErr.contains("no such file") || lowerErr.contains("cannot stat") || lowerErr.contains("cannot access") -> {
                "The target path or folder you requested isn't located here.\n" +
                "💡 Hint: Run `ls` to audit exactly what files exist in your immediate vicinity."
            }
            lowerErr.contains("directory") && lowerErr.contains("is a") -> {
                "You tried running a text operator on a directory stream!\n" +
                "💡 Hint: Select flat files instead, or run `cd ${args.firstOrNull() ?: ""}` to navigate inside."
            }
            lowerErr.contains("permission") || lowerErr.contains("denied") -> {
                "Ah! Secure Access restrictions are configured on this backup script.\n" +
                "💡 Hint: Try adding execute permissions with `chmod +x ${args.firstOrNull() ?: ""}` to unlock it!"
            }
            cmd == "mkdir" && lowerErr.contains("exists") -> {
                "A folder with that exact name already exists here!\n" +
                "💡 Hint: Pick a unique name, e.g. `mkdir project_beta`."
            }
            cmd == "rm" && lowerErr.contains("directory") -> {
                "Standard 'rm' can only remove files. To remove folders, we need recursive flags.\n" +
                "💡 Hint: Run `rm -rf ${args.lastOrNull() ?: ""}` to forcefully remove directories recursively!"
            }
            else -> {
                "Something went askew. Here is an AI tutor hint: double check command spelling parameters, paths, or type `help $cmd` to see usage structures! You've got this."
            }
        }
    }
}
