import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int amountOfTestCases = Integer.parseInt(scanner.nextLine());
        Main main = new Main();

        for(int i = 0; i < amountOfTestCases; i++) {
            String[] rowsAndCols = scanner.nextLine().split(" ");
            int rows = Integer.parseInt(rowsAndCols[1]);
            int cols = Integer.parseInt(rowsAndCols[0]);

            char[][] maze = new char[rows][cols];
            StringBuilder mazeText = new StringBuilder();
            for(int j = 0; j < rows; j++) {
                int length = mazeText.length();
                mazeText.append(scanner.nextLine());
                for(int k = 0; k < mazeText.length() - length; k++) {
                    maze[j][k] = mazeText.charAt(length + k);
                }
            }

            Graph graph = main.getGraphFromMaze(maze);
            graph.setRows(rows);
            graph.setCols(cols);
            graph.setWeights();
            System.out.println(graph.kruskal());
        }
    }

    public Graph getGraphFromMaze(char[][] maze) {

        HashMap<String, ArrayList<Edge>> vertices = new HashMap<>();
        HashMap<String, Character> vertexValues = new HashMap<>();

        for(int i = 0; i < maze.length; i++) {
            for(int j = 0; j < maze[i].length; j++) {

                if(maze[i][j] != '#' && maze[i][j] != '\u0000') {
                    ArrayList<Edge> edges = new ArrayList<>();
                    vertexValues.put("" + i + "," + j, maze[i][j]);

                    if(j - 1 > 0) {
                        if(maze[i][j - 1] != '#' && maze[i][j - 1] != '\u0000') {
                            edges.add(new Edge("" + i + "," + j, "" + i + "," + (j - 1)));
                        }
                    }

                    if(i - 1 > 0) {
                        if(maze[i - 1][j] != '#' && maze[i - 1][j] != '\u0000') {
                            edges.add(new Edge("" + i + "," + j, "" + (i - 1) + "," + j));
                        }
                    }

                    if(j + 1 < maze[i].length) {
                        if(maze[i][j + 1] != '#' && maze[i][j + 1] != '\u0000') {
                            edges.add(new Edge("" + i + "," + j, "" + i + "," + (j + 1)));
                        }
                    }

                    if(i + 1 < maze.length) {
                        if(maze[i + 1][j] != '#' && maze[i + 1][j] != '\u0000') {
                            edges.add(new Edge("" + i + "," + j, "" + (i + 1) + "," + j));
                        }
                    }

                    vertices.put("" + i + "," + j, edges);
                }
            }
        }

        Graph graph = new Graph(vertices);
        graph.setVertexValues(vertexValues);

        return graph;
    }

}

class Graph {

    private HashMap<String, ArrayList<Edge>> vertices;
    private HashMap<String, Character> vertexValues;
    private ArrayList<Edge> allEdges;
    private int rows;
    private int cols;


    public Graph(HashMap<String, ArrayList<Edge>> vertices) {
        this.vertices = vertices;
        allEdges = new ArrayList<>();
    }

    public void setWeights() {
        HashMap<String, ArrayList<Edge>> updatedGraph = new HashMap<>();

        for (String vertex : vertices.keySet()) {
            if(vertexValues.get(vertex) != ' ') {
                ArrayList<Edge> updatedEdges = bfs(vertex);
                updatedGraph.put(vertex, updatedEdges);
            }
        }

        this.vertices = updatedGraph;
    }

    public ArrayList<Edge> bfs(String root) {
        Queue<String> vertexQueue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][cols];

        String[] rowsAndCols = root.split(",");
        int rootRows = Integer.parseInt(rowsAndCols[0]);
        int rootCols = Integer.parseInt(rowsAndCols[1]);
        visited[rootRows][rootCols] = true;
        vertexQueue.add(root);

        ArrayList<Edge> updatedEdges = new ArrayList<>();
        HashMap<String, Integer> vertexDistance = new HashMap<>();
        vertexDistance.put(root, 1);

        while(!vertexQueue.isEmpty()) {
            String current = vertexQueue.remove();
            ArrayList<Edge> edges = vertices.get(current);

            for(Edge edge : edges) {
                String vertex = edge.getVertexTo();
                String[] vertexRowsAndCols = vertex.split(",");
                int vertexRows = Integer.parseInt(vertexRowsAndCols[0]);
                int vertexCols = Integer.parseInt(vertexRowsAndCols[1]);
                if(!visited[vertexRows][vertexCols]) {
                    visited[vertexRows][vertexCols] = true;
                    vertexDistance.put(vertex, vertexDistance.get(current) + 1);
                    if(vertexValues.get(vertex) != ' ') {
                        Edge updatedEdge = new Edge(root, vertex);
                        updatedEdge.setWeight(vertexDistance.get(current));
                        updatedEdges.add(updatedEdge);
                        allEdges.add(updatedEdge); //unnecessary to save two different arraylists here TODO fix later
                    }
                    vertexQueue.add(vertex);
                }
            }
        }

        return updatedEdges;
    }

    public int kruskal() {
        DisjointedSet ds = new DisjointedSet(vertices);
        int totalWeight = 0;

        allEdges.sort(new Comparator<Edge>() {
            public int compare(Edge e1, Edge e2) {
                if (e1.getWeight() > e2.getWeight()) {
                    return 1;
                } else if (e1.getWeight() < e2.getWeight()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for(Edge edge : allEdges) {
            String firstParent = ds.find(edge.getVertexFrom());
            String secondParent = ds.find(edge.getVertexTo());
            if(!firstParent.equals(secondParent)) {
                totalWeight += edge.getWeight();
                ds.union(firstParent, secondParent);
            }
        }

        return totalWeight;
    }

    public void setVertexValues(HashMap<String, Character> vertexValues) {
        this.vertexValues = vertexValues;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

}

class DisjointedSet {
    private HashMap<String, String> parents;
    private HashMap<String, Integer> ranks;

    public DisjointedSet(HashMap<String, ArrayList<Edge>> vertices) {
        parents = new HashMap<>();
        ranks = new HashMap<>();

        for(String vertex : vertices.keySet()) {
            parents.put(vertex, vertex);
            ranks.put(vertex, 0);
        }
    }

    public String find(String vertex) {
        if(parents.get(vertex).equals(vertex)) {
            return vertex;
        }

        return find(parents.get(vertex));

    }

    public void union(String firstSet, String secondSet) {
        if(ranks.get(firstSet) > ranks.get(secondSet)) {
            parents.put(secondSet, firstSet);
        } else if(ranks.get(secondSet) > ranks.get(firstSet)) {
            parents.put(firstSet, secondSet);
        } else {
            parents.put(firstSet, secondSet);
            ranks.put(secondSet, ranks.get(secondSet) + 1);
        }
    }

}

class Edge {
    private String vertexFrom;
    private String vertexTo;
    private int weight;

    public Edge(String vertexFrom, String vertexTo) {
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;
    }

    public String getVertexFrom() {
        return vertexFrom;
    }


    public String getVertexTo() {
        return vertexTo;
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
