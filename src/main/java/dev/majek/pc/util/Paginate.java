package dev.majek.pc.util;

import dev.majek.pc.PartyChat;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to paginate long lists of text, such as help, in chat.
 */
public class Paginate {

    private final int pages;
    private int currentPage;
    private final String header;
    private final Map<Integer, String> pageMap;

    // Config values
    public String PAGE = getConfigString("page");
    public String PREV = getConfigString("prev");
    public String PREVIOUS_PAGE = getConfigString("previous-page");
    public String NO_PREVIOUS_PAGE = getConfigString("no-previous-page");
    public String NEXT = getConfigString("next");
    public String NEXT_PAGE = getConfigString("next-page");
    public String NO_NEXT_PAGE = getConfigString("no-next-page");

    /**
     * Generate pagination for when there is a long list of values to display in chat.
     *
     * @param lines         The list of values to display across pages.
     * @param header        The header to display at the top of every page.
     * @param linesPerPage  How many lines to display on each page.
     * @param command       The command, not including page number, to use for moving between pages.
     */
    public Paginate(List<String> lines, String header, int linesPerPage, String command) {
        this.header = header;
        this.pages = lines.size() % linesPerPage == 0 ? lines.size() / linesPerPage : (lines.size() / linesPerPage) + 1;
        this.currentPage = 1;
        this.pageMap = new HashMap<>();

        for (int i = 0; i <= lines.size(); i++) {
            List<String> linesForPage = new ArrayList<>();
            for (int j = i; j <= (i+(linesPerPage-1)); j++) {
                try {
                    linesForPage.add(lines.get(j));
                } catch (Exception ex) {
                    break;
                }
            }
            pageMap.put(currentPage, createPage(linesForPage, currentPage, command));
            currentPage++;
            i += (linesPerPage-1);
        }
    }

    public String getPage() {
        return pageMap.get(currentPage);
    }

    public String getPage(int page) {
        currentPage = page;
        return getPage();
    }

    public String getNextPage() {
        currentPage++;
        return pageMap.get(currentPage);
    }

    public String getPreviousPage() {
        currentPage--;
        return pageMap.get(currentPage);
    }

    public int getMaxPage() {
        return pages;
    }

    /**
     * Create a new page of values. The page formatting is the same as the /help pages.
     *
     * @param lines         The list of values for this page.
     * @param pageNumber    The page number assigned to this page.
     * @param command       The command, not including page number, to use for moving between pages.
     * @return              The created page ready to send formatted.
     */
    public String createPage(List<String> lines, int pageNumber, String command) {
        String page;
        String prevPage = currentPage > 1 ? "${hover-command,&6[&b" + PREV + "&6],&6"+ PREVIOUS_PAGE + ",/"
                + command + " " + (pageNumber - 1) + "} " : "${hover,&6[&7" + PREV + "&6],&6" + NO_PREVIOUS_PAGE + "} ";
        String title = this.header + " - " + PAGE + " " + pageNumber + "/" + pages;
        String nextPage = this.currentPage == pages ? " ${hover,&6[&7" + NEXT + "&6],&6" + NO_NEXT_PAGE + "}"
                : " ${hover-command,&6[&b" + NEXT + "&6],&6"+ NEXT_PAGE + ",/"
                + command + " " + (pageNumber + 1) + "}";
        page = prevPage + title + nextPage;
        StringBuilder buildLines = new StringBuilder();
        for (String line : lines)
            buildLines.append("\n").append(line);
        page = page + buildLines.toString();
        return page;
    }

    public String getConfigString(String path) {
        return PartyChat.getDataHandler().getConfigString(PartyChat.getDataHandler().messages, path);
    }
}