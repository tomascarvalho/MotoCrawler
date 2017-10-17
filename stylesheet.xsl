<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <html>
          <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous"/>
            <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js" integrity="sha384-vBWWzlZJ8ea9aCX4pEW3rVHjgjt7zpkNpZk+02D9phzyeVkE+jo0ieGizqPLForn" crossorigin="anonymous"></script>
            <body>
                <h1>HTML Summary</h1>
                <table border="1" class="table table-striped table-hover">
                    <thead class="thead-inverse">
                        <tr>
                            <th>Imagem</th>
                            <th>Preço</th>
                            <th>Marca</th>
                            <th>Modelo</th>
                            <th>Cor</th>
                            <th>Ano</th>
                            <th>Mês</th>
                            <th>Cilindrada</th>
                            <th>Quilómetros</th>
                            <th>Extras</th>
                      </tr>
                    </thead>
                    <xsl:for-each select="advertisements/advert">
                        <xsl:sort select="price" order="ascending" data-type="number"/>
                        <tr>
                            <td>
                                <a target="_blank">
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="url"/>
                                    </xsl:attribute>
                                    <img height="98" width="148">
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="imageUrl"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="price" />
                                <xsl:value-of select="price/@units" />

                            </td>
                            <td>
                                <xsl:value-of select="brand" />
                            </td>
                            <td>
                                <xsl:value-of select="model" />
                            </td>
                            <td>
                                <xsl:value-of select="color" />
                            </td>
                            <td>
                                <xsl:value-of select="year" />
                            </td>
                            <td>
                                <xsl:value-of select="month" />
                            </td>
                            <td>
                                <xsl:value-of select="displacement" />
                                <xsl:value-of select="displacement/@units" />
                            </td>
                            <td>
                                <xsl:value-of select="mileage" />
                                <xsl:value-of select="mileage/@units" />
                            </td>
                            <xsl:for-each select="extras/extra">
                                    <xsl:value-of select="extra"/>
                            </xsl:for-each>

                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
