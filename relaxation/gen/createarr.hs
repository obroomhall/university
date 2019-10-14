import Data.List
import Control.Monad
import System.Random
import System.IO

getRandomRow :: Int -> IO [Int]
getRandomRow n = sequence $ replicate (n*n) $ randomRIO (1,9::Int)

main = do
  putStrLn "Please enter the dimension of the array"
  inputInt <- getLine
  let dim = read inputInt :: Int
  outh <- openFile dim ++ "randarr.txt" WriteMode
  arr <- getRandomRow dim
  let str = unwords $ map show arr
  hPutStrLn outh str
  hClose outh



