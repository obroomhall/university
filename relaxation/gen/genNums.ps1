function genNumFile($dimension) {

    $stopwatch =  [system.diagnostics.stopwatch]::StartNew()

    $sb = [System.Text.StringBuilder]::new()
    for ($i = 0; $i -lt $dimension*$dimension; $i++) {

        $numStr = (Get-Random -Minimum 0 -Maximum 10).ToString()
        [void]$sb.Append($numStr)
        [void]$sb.Append(' ')
    }

    $largeStr = $sb.ToString()
    $sw = new-object system.IO.StreamWriter("H:\git\Relaxation\$dimension`x$dimension`.txt")
    $sw.write($largeStr)
    $sw.close()

    $stopwatch.Stop()
    $stopwatch.Elapsed;

}