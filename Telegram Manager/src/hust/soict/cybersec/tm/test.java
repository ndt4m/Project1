package hust.soict.cybersec.tm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wagu.Block;
import wagu.Board;
import wagu.Table;

public class test 
{
    public static void main(String[] args)
    {
        List<String> headersList = Arrays.asList("User ID", "First Name", "Last Name", "UserName", "Phone Number", "IsScam", "IsFake", "User Type");
        List<List<String>> rowsList = Arrays.asList(
        Arrays.asList("6054221228", "Ho\u00E0ng Anh IT-E15", "", "", "84862642048", "No", "No", "regular user" ),
        Arrays.asList("2134816269", "xbxbxbxv", "oirbfbc", "fapspfkjc", "84857864831", "No", "No", "regular user"),
        Arrays.asList("806954250", "\u2018", "", "znfeau", "", "No", "No", "regular user"),
        Arrays.asList("1817109845", "\u0110\u1ED3ng", "L\u00EA", "LeDong97", "", "No", "No", "regular user")
        );
        Board board = new Board(160);
        Table table = new Table(board, 160, headersList, rowsList);
        table.getColWidthsList();
        List<Integer> colAlignList = Arrays.asList(
            Block.DATA_CENTER, 
            Block.DATA_CENTER, 
            Block.DATA_CENTER, 
            Block.DATA_CENTER,
            Block.DATA_CENTER,
            Block.DATA_CENTER,
            Block.DATA_CENTER, 
            Block.DATA_CENTER);
        table.setColAlignsList(colAlignList);
        List<Integer> colWidthsListEdited = Arrays.asList(20, 20, 20, 20, 15, 6, 6, 20);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        System.out.println(tableString);



    }
}
