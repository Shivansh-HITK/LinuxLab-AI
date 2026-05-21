package com.example.filesystem

import java.util.Stack

sealed class VNode {
    abstract val name: String
    abstract var permissions: String // e.g. "rwxr-xr-x" or "rw-r--r--"
}

data class VFile(
    override val name: String,
    var content: String,
    override var permissions: String = "rw-r--r--"
) : VNode()

class VDirectory(
    override val name: String,
    val parent: VDirectory?,
    val children: MutableMap<String, VNode> = mutableMapOf(),
    override var permissions: String = "rwxr-xr-x"
) : VNode() {
    
    fun getPath(): String {
        if (parent == null) return "/"
        val parentPath = parent.getPath()
        return if (parentPath == "/") "/$name" else "$parentPath/$name"
    }

    fun cloneTree(newParent: VDirectory?): VDirectory {
        val dir = VDirectory(name, newParent, permissions = permissions)
        children.forEach { (k, v) ->
            when (v) {
                is VFile -> dir.children[k] = v.copy()
                is VDirectory -> dir.children[k] = v.cloneTree(dir)
            }
        }
        return dir
    }
}

class VirtualFileSystem {
    val root = VDirectory("root", null)
    var currentDir: VDirectory = root

    init {
        resetToDefault()
    }

    fun resetToDefault() {
        root.children.clear()
        currentDir = root
        
        // Add welcome text
        root.children["welcome.txt"] = VFile(
            "welcome.txt",
            "=========================================\n" +
            "🌟 Welcome to LinuxLab AI Terminal! 🌟\n" +
            "=========================================\n" +
            "You are in a fully secure, simulated educational Linux environment.\n" +
            "Type 'help' to see available learning options,\n" +
            "or check out our missions in the Missions Tab!\n\n" +
            "Try starting with: 'ls' to see files, or 'cat welcome.txt'.\n" +
            "Enjoy learning!",
            "rw-r--r--"
        )

        root.children["about_me.txt"] = VFile(
            "about_me.txt",
            "🧑‍💻 User: Student of LinuxLab AI\n" +
            "📈 Objective: Master the command terminal\n" +
            "⭐ Status: Rookie (Level 1)\n" +
            "Try running: 'grep Objective about_me.txt' to search details!",
            "rw-r--r--"
        )

        // Projects directory
        val projects = VDirectory("projects", root)
        projects.children["todo.py"] = VFile(
            "todo.py",
            "def main():\n" +
            "    print(\"TODO: Implement automatic server script!\")\n" +
            "    print(\"TODO: Master permissions with chmod!\")\n",
            "rw-r--r--"
        )
        projects.children["draft.txt"] = VFile(
            "draft.txt",
            "First draft of secret project details.\nKeep away from system admins.",
            "rw-r--r--"
        )
        root.children["projects"] = projects

        // Scripts directory
        val scripts = VDirectory("scripts", root)
        scripts.children["backup.sh"] = VFile(
            "backup.sh",
            "#!/bin/bash\necho 'Backing up user folders...'\ntar -czf backup.tar.gz ../projects/",
            "rw-------" // Read/write only for owner
        )
        scripts.children["run_tests.sh"] = VFile(
            "run_tests.sh",
            "#!/bin/bash\necho 'Running sandbox tests...'\necho 'Success!'",
            "rw-r--r--" // Needs chmod +x to be runnable in the mind of learner!
        )
        root.children["scripts"] = scripts

        // Sandbox directory (empty directory)
        val sandbox = VDirectory("sandbox", root)
        root.children["sandbox"] = sandbox
    }

    // Resolve path to VNode relative to current directory
    fun resolveNode(path: String): VNode? {
        if (path.isEmpty()) return currentDir
        
        val segments = path.split("/").filter { it.isNotEmpty() }
        var temp: VNode = if (path.startsWith("/")) root else currentDir

        for (seg in segments) {
            if (temp !is VDirectory) return null
            when (seg) {
                "." -> {} // current dir
                ".." -> temp = temp.parent ?: root
                else -> {
                    temp = temp.children[seg] ?: return null
                }
            }
        }
        return temp
    }

    // Helper to get parent directory of a path and the target name
    private fun resolveParentAndName(path: String): Pair<VDirectory?, String>? {
        if (path.isEmpty()) return null
        val cleanPath = path.trimEnd('/')
        val idx = cleanPath.lastIndexOf('/')
        return if (idx == -1) {
            Pair(currentDir, cleanPath)
        } else {
            val parentPath = if (idx == 0) "/" else cleanPath.substring(0, idx)
            val name = cleanPath.substring(idx + 1)
            val parentNode = resolveNode(parentPath)
            if (parentNode is VDirectory) {
                Pair(parentNode, name)
            } else {
                null
            }
        }
    }

    // Commands implementation (All commands return String output representation)

    fun pwd(): String {
        return currentDir.getPath()
    }

    fun ls(path: String = ""): List<Pair<String, Boolean>> { // Name and isDirectory
        val targetNode = if (path.isEmpty()) currentDir else resolveNode(path)
            ?: throw Exception("ls: cannot access '$path': No such file or directory")
        
        return if (targetNode is VDirectory) {
            targetNode.children.values.sortedBy { it.name }.map { Pair(it.name, it is VDirectory) }
        } else {
            listOf(Pair(targetNode.name, false))
        }
    }

    fun cd(path: String): String {
        if (path.isEmpty() || path == "~") {
            currentDir = root
            return ""
        }
        val targetNode = resolveNode(path)
            ?: throw Exception("cd: $path: No such file or directory")
        
        if (targetNode !is VDirectory) {
            throw Exception("cd: not a directory: $path")
        }
        currentDir = targetNode
        return ""
    }

    fun mkdir(path: String): String {
        val pair = resolveParentAndName(path)
            ?: throw Exception("mkdir: cannot create directory '$path': No such file or directory")
        
        val parent = pair.first ?: throw Exception("mkdir: cannot create directory: parent not found")
        val name = pair.second
        
        if (parent.children.containsKey(name)) {
            throw Exception("mkdir: cannot create directory '$name': File exists")
        }
        
        parent.children[name] = VDirectory(name, parent)
        return ""
    }

    fun touch(path: String): String {
        val pair = resolveParentAndName(path)
            ?: throw Exception("touch: cannot touch '$path': No such file or directory")
        
        val parent = pair.first ?: throw Exception("touch: cannot touch: parent not found")
        val name = pair.second
        
        val existing = parent.children[name]
        if (existing is VFile) {
            // Update performance
            return ""
        } else if (existing is VDirectory) {
            throw Exception("touch: '$name' is a directory")
        }
        
        parent.children[name] = VFile(name, "")
        return ""
    }

    fun rm(path: String, recursive: Boolean = false): String {
        val pair = resolveParentAndName(path)
            ?: throw Exception("rm: cannot remove '$path': No such file or directory")
        
        val parent = pair.first ?: throw Exception("rm: parent not found")
        val name = pair.second
        
        val node = parent.children[name] ?: throw Exception("rm: cannot remove '$path': No such file or directory")
        
        if (node is VDirectory && !recursive) {
            throw Exception("rm: cannot remove '$path': Is a directory")
        }
        
        parent.children.remove(name)
        return ""
    }

    fun cat(path: String): String {
        val node = resolveNode(path) ?: throw Exception("cat: $path: No such file or directory")
        if (node is VDirectory) {
            throw Exception("cat: $path: Is a directory")
        }
        return (node as VFile).content
    }

    fun echo(content: String, redirectPath: String? = null, append: Boolean = false): String {
        if (redirectPath == null) {
            return content
        }
        // Redirect logic
        val pair = resolveParentAndName(redirectPath)
            ?: throw Exception("echo: cannot write to '$redirectPath': No such file or directory")
        
        val parent = pair.first ?: throw Exception("echo: cannot write to: parent not found")
        val name = pair.second
        
        val node = parent.children[name]
        if (node is VDirectory) {
            throw Exception("echo: '$redirectPath' is a directory")
        }
        
        val existingFile = node as? VFile
        if (existingFile != null) {
            if (append) {
                existingFile.content = if (existingFile.content.endsWith("\n") || existingFile.content.isEmpty()) {
                    existingFile.content + content
                } else {
                    existingFile.content + "\n" + content
                }
            } else {
                existingFile.content = content
            }
        } else {
            parent.children[name] = VFile(name, content)
        }
        return ""
    }

    fun mv(srcPath: String, destPath: String): String {
        val srcNode = resolveNode(srcPath)
            ?: throw Exception("mv: cannot stat '$srcPath': No such file or directory")
        
        val srcPair = resolveParentAndName(srcPath) ?: throw Exception("mv: invalid source path")
        val srcParent = srcPair.first ?: throw Exception("mv: source parent not found")
        val srcName = srcPair.second

        val destNode = resolveNode(destPath)
        
        if (destNode is VDirectory) {
            // Move into directory
            if (destNode.children.containsKey(srcName)) {
                throw Exception("mv: cannot overwrite '$destPath/$srcName': File exists")
            }
            // Remove from source parent and set to dest parent after re-contexting VDirectory parents if necessary
            srcParent.children.remove(srcName)
            when (srcNode) {
                is VFile -> destNode.children[srcName] = srcNode
                is VDirectory -> {
                    val reParented = VDirectory(srcName, destNode, srcNode.children, srcNode.permissions)
                    // Re-parent children recursively or simply update reference parent:
                    destNode.children[srcName] = reParented
                }
            }
        } else {
            // Rename
            val destPair = resolveParentAndName(destPath)
                ?: throw Exception("mv: cannot move to '$destPath': parent not found")
            val destParent = destPair.first ?: throw Exception("mv: dest parent not found")
            val destName = destPair.second

            srcParent.children.remove(srcName)
            when (srcNode) {
                is VFile -> {
                    val renamed = srcNode.copy(name = destName)
                    destParent.children[destName] = renamed
                }
                is VDirectory -> {
                    val renamed = VDirectory(destName, destParent, srcNode.children, srcNode.permissions)
                    destParent.children[destName] = renamed
                }
            }
        }
        return ""
    }

    fun cp(srcPath: String, destPath: String): String {
        val srcNode = resolveNode(srcPath)
            ?: throw Exception("cp: cannot stat '$srcPath': No such file or directory")
        
        val srcPair = resolveParentAndName(srcPath) ?: throw Exception("cp: invalid source path")
        val srcName = srcPair.second

        val destNode = resolveNode(destPath)
        
        if (destNode is VDirectory) {
            if (destNode.children.containsKey(srcName)) {
                throw Exception("cp: cannot overwrite '$destPath/$srcName': File exists")
            }
            when (srcNode) {
                is VFile -> destNode.children[srcName] = srcNode.copy()
                is VDirectory -> {
                    destNode.children[srcName] = srcNode.cloneTree(destNode)
                }
            }
        } else {
            val destPair = resolveParentAndName(destPath)
                ?: throw Exception("cp: cannot copy to '$destPath': parent not found")
            val destParent = destPair.first ?: throw Exception("cp: dest parent not found")
            val destName = destPair.second

            when (srcNode) {
                is VFile -> {
                    destParent.children[destName] = srcNode.copy(name = destName)
                }
                is VDirectory -> {
                    destParent.children[destName] = srcNode.cloneTree(destParent)
                }
            }
        }
        return ""
    }

    fun chmod(permissionsStr: String, path: String): String {
        val node = resolveNode(path)
            ?: throw Exception("chmod: cannot access '$path': No such file or directory")
        
        // Simple validation or accept common inputs like 755, +x, u+rwx
        if (permissionsStr == "+x" || permissionsStr == "u+x" || permissionsStr == "a+x") {
            // make executable
            val base = node.permissions
            val builder = StringBuilder()
            builder.append(base.substring(0, 3).replace('-', 'x')) // Owner part
            builder.append(if (permissionsStr == "a+x") base.substring(3, 9).replace('-', 'x') else base.substring(3, 9))
            node.permissions = builder.toString()
        } else if (permissionsStr.length == 3 && permissionsStr.all { it.isDigit() }) {
            // Numeric parsing e.g. 755
            val formatted = translateNumericToSymbolic(permissionsStr)
            node.permissions = formatted
        } else {
            // Set custom permissions
            node.permissions = permissionsStr
        }
        return ""
    }

    private fun translateNumericToSymbolic(num: String): String {
        val symbols = listOf("---", "--x", "-w-", "-wx", "r--", "r-x", "rw-", "rwx")
        val owner = symbols.getOrNull(num[0].toString().toInt()) ?: "rwx"
        val group = symbols.getOrNull(num[1].toString().toInt()) ?: "r-x"
        val other = symbols.getOrNull(num[2].toString().toInt()) ?: "r-x"
        return owner + group + other
    }

    fun grep(pattern: String, path: String): String {
        val node = resolveNode(path)
            ?: throw Exception("grep: $path: No such file or directory")
        
        if (node is VDirectory) {
            throw Exception("grep: $path: Is a directory")
        }
        
        val fileContent = (node as VFile).content
        val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
        val matchingLines = fileContent.lines().filter { it.contains(regex) }
        
        return if (matchingLines.isEmpty()) "" else matchingLines.joinToString("\n")
    }

    fun find(namePattern: String): String {
        val results = mutableListOf<String>()
        fun recurse(dir: VDirectory, currentPath: String) {
            dir.children.values.forEach { child ->
                val fullPath = if (currentPath == "/") "/${child.name}" else "$currentPath/${child.name}"
                if (child.name.contains(namePattern, ignoreCase = true)) {
                    results.add(fullPath)
                }
                if (child is VDirectory) {
                    recurse(child, fullPath)
                }
            }
        }
        recurse(currentDir, currentDir.getPath())
        return if (results.isEmpty()) "" else results.joinToString("\n")
    }

    fun head(path: String, numLines: Int = 10): String {
        val node = resolveNode(path)
            ?: throw Exception("head: cannot open '$path' for reading: No such file or directory")
        if (node is VDirectory) {
            throw Exception("head: error reading '$path': Is a directory")
        }
        val file = node as VFile
        return file.content.lines().take(numLines).joinToString("\n")
    }

    fun tail(path: String, numLines: Int = 10): String {
        val node = resolveNode(path)
            ?: throw Exception("tail: cannot open '$path' for reading: No such file or directory")
        if (node is VDirectory) {
            throw Exception("tail: error reading '$path': Is a directory")
        }
        val file = node as VFile
        val lines = file.content.lines()
        return lines.takeLast(numLines).joinToString("\n")
    }
}
