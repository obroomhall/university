using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using UnityEngine;

public class StandardTower : MonoBehaviour
{
    private class Block
    {
        private GameObject blockObject;
        private int id;
        private Vector3 pos;
        private Quaternion rot;
        private GameObject block;
        public float score { get; set; }

        public Block(GameObject blockObject, int id, Vector3 pos, Quaternion rot)
        {
            this.blockObject = blockObject;
            this.id = id;
            this.pos = pos;
            this.rot = rot;
            Create();
            CorrectRotationX();
        }

        public int GetID()
        {
            return id;
        }

        public bool isActive()
        {
            return (block != null);
        }

        public void ToggleKinematic()
        {
            var rigidBody = block.GetComponent<Rigidbody>();
            rigidBody.isKinematic = !rigidBody.isKinematic;
        }

        private void Create()
        {
            block = Instantiate(blockObject, pos, rot);
            block.name = "Block " + id;
            block.transform.Rotate(new Vector3(90, 90, 0));
        }

        private void CorrectRotationX()
        {
            var euler = block.transform.rotation.eulerAngles;
            block.transform.rotation = Quaternion.Euler(180, euler.y, 180);
        }

        public bool UpdatePositionAndRotation()
        {
            if (pos == block.transform.position && rot == block.transform.rotation)
            {
                return false;
            }
            else
            {
                pos = block.transform.position;
                rot = block.transform.rotation;
                return true;
            }
        }

        public void ToggleActivation()
        {
            block.SetActive(!block.activeSelf);
        }

        public void ResetPose()
        {
            block.transform.position = pos;
            block.transform.rotation = rot;

            var rigidbody = block.GetComponent<Rigidbody>();
            rigidbody.velocity = Vector3.zero;
            rigidbody.angularVelocity = Vector3.zero;
        }

        public float GetMaxDisplacement()
        {
            float x = Math.Abs(block.transform.position.x);
            float y = Math.Abs(block.transform.position.y);
            float z = Math.Abs(block.transform.position.z);
            return Math.Max(x, Math.Max(y, z));
        }

        public Vector3 GetPosition()
        {
            return block.transform.position;
        }

        public Quaternion GetRotation()
        {
            return block.transform.rotation;
        }

        public float GetPositionChange()
        {
            return (pos - block.transform.position).magnitude;
        }

        public void SetPositionYRelative(float up)
        {
            block.transform.position += Vector3.up * up;
        }

        public float GetHeight()
        {
            return block.transform.localScale.y;
        }

        public void SetColor(Color color)
        {
            block.GetComponent<Renderer>().material.color = color;
        }
    }

    private const string filename = "final";

    private List<Block> blocks;
    public GameObject blockObject;
    private Color[] colours = { Color.green, Color.blue, Color.red, Color.cyan, Color.magenta, Color.yellow };

    private bool towerSettled = false;

    private const float waitTimeInSeconds = 5;
    
    // Start is called before the first frame update
    void Start()
    {
        blocks = new List<Block>();
        var markers = ConvertMapToHashtable(File.ReadAllText("Assets/" + filename + ".yml"));

        //var pose = poses[0];
        foreach (DictionaryEntry marker in markers)
        {
            int id = (int) marker.Key;

            if (id % 2 == 0 && markers.ContainsKey(id+1))
            {
                Vector3[] m = (Vector3[]) marker.Value;
                Vector3[] n = (Vector3[]) markers[id+1];

                Vector3 mCenter = GetMidpoint(m[0], m[2]);
                Vector3 nCenter = GetMidpoint(n[0], n[2]);
                //Debug.DrawLine(mCenter, nCenter, Color.red, 100);

                Vector3 connectingVector = mCenter - nCenter;

                float scale = blockObject.transform.localScale.x / connectingVector.magnitude;
                mCenter *= scale;
                nCenter *= scale;
                connectingVector *= scale;

                Block block = new Block(blockObject, id, GetMidpoint(mCenter, nCenter), Quaternion.LookRotation(connectingVector, Vector3.forward));
                //block.ToggleKinematic();
                blocks.Add(block);
            }
        }

        // bring all blocks to 0 height
        float lowestBlock = blocks.Min(b => b.GetPosition().y);
        float blockHeight = blockObject.transform.localScale.y;
        foreach (var block in blocks)
        {
            block.SetPositionYRelative(0 - (lowestBlock - blockHeight/2));
        }

        StartCoroutine(WaitForSettled());
    }

    private IEnumerator WaitForSettled()
    {
        int unsettledCount = 0;

        // Check if any blocks are still moving
        while (blocks.Where(b => b.UpdatePositionAndRotation()).Count() > 0)
        {
            print("Tower unsettled");
            yield return new WaitForSeconds(1);
            unsettledCount++;

            if (unsettledCount > 10)
            {
                throw new Exception("Tower did not settle");
            }
        }

        print("Tower settled");
        towerSettled = true;
    }

    private int?[,] getBlockIds()
    {
        return new int?[18, 3]
        {
            { 1,    2,   3 },
            { 4,    5,   6 },
            { null,    8,   null },
            { 10,   11,  12 },
            { 13,   14,  15 },
            { 16,   null,  18 },
            { 19,   null,  21 },
            { 22,   null,  24 },
            { 25,   26,  27 },
            { 28,   29,  30 },
            { 31,   32,  33 },
            { 34,   35,  36 },
            { null,   38,  39 },
            { 40,   41,  42 },
            { 43,   44,  45 },
            { 46,   47,  48 },
            { 49,   50,  51 },
            { 52,   53,  54 }
        };
    }

    // https://stackoverflow.com/questions/11734380/check-for-null-in-foreach-loop
    //public static IEnumerable<T> OrEmptyIfNull<T>(this IEnumerable<T> source)
    //{
    //    return source ?? Enumerable.Empty<T>();
    //}

    private void SetBlocksFromIds(int?[,] blockIds)
    {
        int blockRows = blockIds.GetLength(0);
        int blocksPerRow = blockIds.GetLength(1);

        float blockHeight = blockObject.transform.localScale.y;
        float blockWidth = blockObject.transform.localScale.z;

        blocks = new List<Block>();

        for (int row = 0; row < blockRows; row++)
        {
            float vDisp = blockHeight / 2 + row * blockHeight;

            for (int col = 0; col < blocksPerRow; col++)
            {
                if (blockIds[row,col] == null)
                {
                    continue;
                }

                float hDisp = (col - 1) * blockWidth * 1.05f;

                Vector3 pos;
                Quaternion rot;

                // Even rows
                if (row % 2 == 0)
                {
                    pos = new Vector3(1, vDisp+1, hDisp+1);
                    rot = Quaternion.Euler(0, 0, 0);
                }
                // Odd rows
                else
                {
                    pos = new Vector3(hDisp+1, vDisp+1, 1);
                    rot = Quaternion.Euler(0, 90, 0);
                }

                blocks.Add(new Block(blockObject, blockIds[row, col].GetValueOrDefault(), pos, rot));//, colours[row % 2 + col % 3]));
            }
        }
    }

    private IEnumerator RemoveOneByOneAnalysis()
    {

        foreach (var block in blocks)
        {
            block.ToggleActivation();
            yield return new WaitForSeconds(waitTimeInSeconds);

            block.score = blocks.Sum(b => b.GetPositionChange());

            print("Score: " + block.score);
            block.ToggleActivation();

            foreach (var movedBlock in blocks)
            {
                movedBlock.ResetPose();
            }
        }

        DrawColours();
        Time.timeScale = 0;

        using (StreamWriter file = new StreamWriter(filename + "_ranks.csv"))
        {
            file.WriteLine("id,score");
            foreach (var block in blocks)
            {
                file.WriteLine(block.GetID() + "," + block.score);
            }
        }
    }

    // takes advantage of rgb values being restricted to 1
    private Color GetColourFromNorm(float norm)
    {
        return new Color(2 * norm, 2 * (1 - norm), 0);
    }

    private void DrawColours()
    {
        var sorted = blocks.OrderBy(b => b.score);
        float size = sorted.Count();

        for (int i = 0; i < size; i++)
        {
            float rank = i / size;
            sorted.ElementAt(i).SetColor(GetColourFromNorm(rank));
        }
    }

    private void DrawColoursNorm()
    {
        float min = blocks.Min(b => b.score);
        float max = blocks.Max(b => b.score);

        foreach (var block in blocks)
        {
            float norm = (block.score - min) / (max - min);
            block.SetColor(GetColourFromNorm(norm));
        }
    }

    private Vector3 GetMidpoint(Vector3 v, Vector3 u)
    {
        var w = new Vector3();
        for (int i = 0; i < 3; i++)
        {
            w[i] = (v[i] + u[i]) / 2;
        }
        return w;
    }

    private Hashtable ConvertMapToHashtable(string map)
    {
        var idsAndCorners = new Hashtable();
        var idsAndLocsStrArr = map.Split('{', '}');
        for (int i = 1; i < idsAndLocsStrArr.Length-1; i+=2)
        {
            var id = int.Parse(idsAndLocsStrArr[i].Split(',')[0].Split(':')[1]);
            var locs = idsAndLocsStrArr[i].Split('[', ']');
            
            var corners = new Vector3[4];
            for (int j = 0; j < 4; j++)
            {
                var coords = locs[j*2+2].Split(',');
                for (int k = 0; k < 3; k++)
                {
                    corners[j][k] = float.Parse(coords[k]);
                }
            }

            idsAndCorners.Add(id, corners);
        }
        return idsAndCorners;
    }

    // Update is called once per frame
    void Update()
    {
        if (towerSettled)
        {
            StartCoroutine(RemoveOneByOneAnalysis());
            towerSettled = false;
        }
    }
}
