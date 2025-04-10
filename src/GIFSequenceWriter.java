import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.util.Iterator;
import javax.imageio.ImageTypeSpecifier;

public class GIFSequenceWriter {
    protected ImageWriter gifWriter;
    protected ImageWriteParam imageWriteParam;
    protected IIOMetadata imageMetaData;

    public GIFSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesMS, boolean loopContinuously) throws IOException {
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appExtension = new IIOMetadataNode("ApplicationExtension");

        appExtension.setAttribute("applicationID", "NETSCAPE");
        appExtension.setAttribute("authenticationCode", "2.0");

        int loop = loopContinuously ? 0 : 1;
        appExtension.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)});
        appExtensionsNode.appendChild(appExtension);
        root.appendChild(appExtensionsNode);

        imageMetaData.setFromTree(metaFormatName, root);
        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    public void writeToSequence(RenderedImage img) throws IOException {
        gifWriter.writeToSequence(new javax.imageio.IIOImage(img, null, imageMetaData), imageWriteParam);
    }

    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    private static ImageWriter getWriter() throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("gif");
        if (!writers.hasNext()) {
            throw new IOException("No GIF Image Writers Exist");
        }
        return writers.next();
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}